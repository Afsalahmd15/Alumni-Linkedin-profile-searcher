
package com.example.alumni.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.alumni.model.University;
import com.example.alumni.repository.UniversityRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class UniversityServiceTest {

    private UniversityRepository universityRepository;
    private BridglyService bridglyService;
    private UniversityService universityService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        universityRepository = mock(UniversityRepository.class);
        bridglyService = mock(BridglyService.class);

        universityService = new UniversityService(
                universityRepository,
                bridglyService
        );

        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldReturnExistingUniversity() {

        University university = new University();
        university.setName("Vidya Academy of Science and Technology");
        university.setSchoolId("123");

        when(universityRepository.findByNameIgnoreCase(
                "Vidya Academy of Science and Technology"
        )).thenReturn(Optional.of(university));

        University result = universityService.findOrCreateUniversity(
                "Vidya Academy of Science and Technology"
        );

        assertNotNull(result);
        assertEquals(
                "Vidya Academy of Science and Technology",
                result.getName()
        );

        verify(bridglyService, never()).searchSchools(anyString());
        verify(universityRepository, never()).save(any());
    }

    @Test
    void shouldDiscoverAndSaveUniversity() throws Exception {

        when(universityRepository.findByNameIgnoreCase(
                "Vidya Academy of Science and Technology"
        )).thenReturn(Optional.empty());

        JsonNode response = objectMapper.readTree("""
            {
              "data": {
                "schools": [
                  {
                    "name": "Vidya Academy of Science and Technology",
                    "schoolId": "123",
                    "schoolUrn": "urn:123",
                    "schoolUrl": "https://example.com",
                    "location": "Thrissur"
                  }
                ]
              }
            }
            """);

        when(bridglyService.searchSchools(anyString()))
                .thenReturn(response);

        when(universityRepository.save(any(University.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        University result = universityService.findOrCreateUniversity(
                "Vidya Academy of Science and Technology"
        );

        assertNotNull(result);
        assertEquals(
                "Vidya Academy of Science and Technology",
                result.getName()
        );
        assertEquals("123", result.getSchoolId());
        assertEquals("urn:123", result.getSchoolUrn());
        assertEquals("Thrissur", result.getLocation());

        verify(bridglyService).searchSchools(
                "Vidya Academy of Science and Technology"
        );

        verify(universityRepository).save(any(University.class));
    }

    @Test
    void shouldThrowExceptionWhenNoSchoolsAreFound() throws Exception {

        when(universityRepository.findByNameIgnoreCase(anyString()))
                .thenReturn(Optional.empty());

        JsonNode response = objectMapper.readTree("""
            {
              "data": {
                "schools": []
              }
            }
            """);

        when(bridglyService.searchSchools(anyString()))
                .thenReturn(response);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> universityService.findOrCreateUniversity("Unknown University")
        );

        assertEquals(
                "University not found: Unknown University",
                exception.getMessage()
        );

        verify(universityRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenExactUniversityMatchIsNotFound()
            throws Exception {

        when(universityRepository.findByNameIgnoreCase(anyString()))
                .thenReturn(Optional.empty());

        JsonNode response = objectMapper.readTree("""
            {
              "data": {
                "schools": [
                  {
                    "name": "Another Engineering College",
                    "schoolId": "456"
                  }
                ]
              }
            }
            """);

        when(bridglyService.searchSchools(anyString()))
                .thenReturn(response);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> universityService.findOrCreateUniversity(
                        "Vidya Academy of Science and Technology"
                )
        );

        assertEquals(
                "No exact university match found. Please verify the university name.",
                exception.getMessage()
        );

        verify(universityRepository, never()).save(any());
    }
}
