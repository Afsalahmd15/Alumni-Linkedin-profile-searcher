
package com.example.alumni.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.alumni.model.University;

@DataJpaTest
class UniversityRepositoryTest {

    @Autowired
    private UniversityRepository universityRepository;

    @Test
    void shouldFindUniversityByNameIgnoringCase() {

        University university = new University();
        university.setName("Vidya Academy of Science and Technology");
        university.setSchoolId("123");

        universityRepository.save(university);

        Optional<University> result =
                universityRepository.findByNameIgnoreCase(
                        "vidya academy of science and technology"
                );

        assertTrue(result.isPresent());
        assertEquals(
                "Vidya Academy of Science and Technology",
                result.get().getName()
        );
    }

    @Test
    void shouldFindUniversityBySchoolId() {

        University university = new University();
        university.setName("Vidya Academy");
        university.setSchoolId("123");

        universityRepository.save(university);

        Optional<University> result =
                universityRepository.findBySchoolId("123");

        assertTrue(result.isPresent());
        assertEquals("Vidya Academy", result.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenUniversityDoesNotExist() {

        Optional<University> result =
                universityRepository.findByNameIgnoreCase(
                        "Unknown University"
                );

        assertTrue(result.isEmpty());
    }
}
