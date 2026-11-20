
package com.example.alumni.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.alumni.dto.AlumniResponse;
import com.example.alumni.model.Alumni;
import com.example.alumni.model.AlumniSearch;
import com.example.alumni.repository.AlumniRepository;
import com.example.alumni.repository.AlumniSearchRepository;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class AlumniServiceTest {

    @Mock
    private AlumniRepository alumniRepository;

    @Mock
    private AlumniSearchRepository alumniSearchRepository;

    @Mock
    private PhantomBusterService phantomBusterService;

    @Mock
    private UniversityService universityService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AlumniService alumniService;


    @Test
    void shouldReturnExistingAlumniWhenSearchIsCompleted() {

        // Arrange
        AlumniSearch search = new AlumniSearch();
        search.setId(1L);
        search.setUniversity("Vidya Academy of Science and Technology");
        search.setDesignation("Integration Engineer");
        search.setStatus("COMPLETED");

        Alumni alumni = new Alumni();
        alumni.setName("Aswathy C B");
        alumni.setCurrentRole("Integration Engineer");
        alumni.setUniversity("Vidya Academy of Science and Technology");
        alumni.setLocation("Thrissur");
        alumni.setPassoutYear(2025);

        when(alumniSearchRepository
                .findFirstByUniversityIgnoreCaseAndDesignationIgnoreCaseAndPassoutYear(
                        anyString(), anyString(), isNull()))
                .thenReturn(Optional.of(search));

        when(alumniRepository.findByAlumniSearchId(1L))
                .thenReturn(List.of(alumni));

        var request = new com.example.alumni.dto.AlumniSearchRequest(
                "Vidya Academy of Science and Technology",
                "Integration Engineer",
                null
        );

        // Act
        List<AlumniResponse> result =
                alumniService.searchAlumni(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Aswathy C B", result.get(0).getName());
        assertEquals("Integration Engineer",
                result.get(0).getCurrentRole());

        verify(alumniRepository).findByAlumniSearchId(1L);

        verifyNoInteractions(phantomBusterService);
    }


    @Test
    void shouldThrowExceptionWhenSearchIsAlreadyInProgress() {

        // Arrange
        AlumniSearch search = new AlumniSearch();
        search.setId(1L);
        search.setStatus("IN_PROGRESS");

        when(alumniSearchRepository
                .findFirstByUniversityIgnoreCaseAndDesignationIgnoreCaseAndPassoutYear(
                        anyString(), anyString(), isNull()))
                .thenReturn(Optional.of(search));

        var request = new com.example.alumni.dto.AlumniSearchRequest(
                "Vidya Academy of Science and Technology",
                "Integration Engineer",
                null
        );

        // Act and Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> alumniService.searchAlumni(request)
        );

        assertEquals(
                "This alumni search is already in progress.",
                exception.getMessage()
        );

        verify(alumniSearchRepository, never()).save(any());

        verifyNoInteractions(phantomBusterService);
    }
}
