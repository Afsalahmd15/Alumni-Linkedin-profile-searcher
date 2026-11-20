package com.example.alumni.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.alumni.model.AlumniSearch;

import java.util.Optional;
@Repository 
public interface AlumniSearchRepository extends JpaRepository<AlumniSearch, Long> {
    
   
    Optional<AlumniSearch> findFirstByUniversityIgnoreCaseAndDesignationIgnoreCaseAndPassoutYear(
        String university,
        String designation,
        Integer passoutYear
);
}
