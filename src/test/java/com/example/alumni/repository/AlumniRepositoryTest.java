
package com.example.alumni.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.alumni.model.Alumni;
import com.example.alumni.model.AlumniSearch;

@DataJpaTest
class AlumniRepositoryTest {

    @Autowired
    private AlumniRepository alumniRepository;

    @Autowired
    private AlumniSearchRepository alumniSearchRepository;

    @Test
    void shouldSaveAlumniSuccessfully() {

        AlumniSearch search = new AlumniSearch();
        search.setUniversity("Vidya Academy");
        search.setDesignation("Java Developer");
        search.setStatus("COMPLETED");

        AlumniSearch savedSearch = alumniSearchRepository.save(search);

        Alumni alumni = new Alumni();
        alumni.setName("Afsal");
        alumni.setCurrentRole("Java Developer");
        alumni.setUniversity("Vidya Academy");
        alumni.setLinkedinProfileUrl(
                "https://linkedin.com/in/test-afsal"
        );
        alumni.setAlumniSearch(savedSearch);

        Alumni result = alumniRepository.save(alumni);

        assertNotNull(result.getId());
        assertEquals("Afsal", result.getName());
        assertEquals("Java Developer", result.getCurrentRole());
    }

    @Test
    void shouldFindAlumniBySearchId() {

        AlumniSearch search = new AlumniSearch();
        search.setUniversity("Vidya Academy");
        search.setDesignation("Java Developer");
        search.setStatus("COMPLETED");

        AlumniSearch savedSearch = alumniSearchRepository.save(search);

        Alumni alumni = new Alumni();
        alumni.setName("Afsal");
        alumni.setCurrentRole("Java Developer");
        alumni.setUniversity("Vidya Academy");
        alumni.setLinkedinProfileUrl(
                "https://linkedin.com/in/test-afsal"
        );
        alumni.setAlumniSearch(savedSearch);

        alumniRepository.save(alumni);

        List<Alumni> result =
                alumniRepository.findByAlumniSearchId(savedSearch.getId());

        assertEquals(1, result.size());
        assertEquals("Afsal", result.get(0).getName());
    }

    @Test
    void shouldReturnEmptyListWhenNoAlumniFound() {

        List<Alumni> result =
                alumniRepository.findByAlumniSearchId(999L);

        assertTrue(result.isEmpty());
    }
}
