
package com.example.alumni.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.alumni.model.AlumniSearch;

@DataJpaTest
class AlumniSearchRepositoryTest {

    @Autowired
    private AlumniSearchRepository alumniSearchRepository;

    @Test
    void shouldFindSearchByUniversityDesignationAndYear() {

        AlumniSearch search = new AlumniSearch();
        search.setUniversity("Vidya Academy");
        search.setDesignation("Java Developer");
        search.setPassoutYear(2025);
        search.setStatus("COMPLETED");

        alumniSearchRepository.save(search);

        Optional<AlumniSearch> result =
                alumniSearchRepository
                        .findFirstByUniversityIgnoreCaseAndDesignationIgnoreCaseAndPassoutYear(
                                "vidya academy",
                                "java developer",
                                2025
                        );

        assertTrue(result.isPresent());
        assertEquals("Vidya Academy", result.get().getUniversity());
        assertEquals("Java Developer", result.get().getDesignation());
        assertEquals(2025, result.get().getPassoutYear());
    }

    @Test
    void shouldReturnEmptyWhenSearchDoesNotExist() {

        Optional<AlumniSearch> result =
                alumniSearchRepository
                        .findFirstByUniversityIgnoreCaseAndDesignationIgnoreCaseAndPassoutYear(
                                "Unknown University",
                                "Developer",
                                2020
                        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenPassoutYearDoesNotMatch() {

        AlumniSearch search = new AlumniSearch();
        search.setUniversity("Vidya Academy");
        search.setDesignation("Java Developer");
        search.setPassoutYear(2025);
        search.setStatus("COMPLETED");

        alumniSearchRepository.save(search);

        Optional<AlumniSearch> result =
                alumniSearchRepository
                        .findFirstByUniversityIgnoreCaseAndDesignationIgnoreCaseAndPassoutYear(
                                "Vidya Academy",
                                "Java Developer",
                                2024
                        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindSearchWithNullPassoutYear() {

        AlumniSearch search = new AlumniSearch();
        search.setUniversity("Vidya Academy");
        search.setDesignation("Java Developer");
        search.setPassoutYear(null);
        search.setStatus("COMPLETED");

        alumniSearchRepository.save(search);

        Optional<AlumniSearch> result =
                alumniSearchRepository
                        .findFirstByUniversityIgnoreCaseAndDesignationIgnoreCaseAndPassoutYear(
                                "Vidya Academy",
                                "Java Developer",
                                null
                        );

        assertTrue(result.isPresent());
        assertNull(result.get().getPassoutYear());
    }
}
