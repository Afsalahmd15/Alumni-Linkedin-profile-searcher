
package com.example.alumni.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.alumni.model.University;

public interface UniversityRepository
        extends JpaRepository<University, Long> {

    Optional<University> findByNameIgnoreCase(String name);

    Optional<University> findBySchoolId(String schoolId);
}
