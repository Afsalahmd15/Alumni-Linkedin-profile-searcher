
package com.example.alumni.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.alumni.model.Alumni;

@Repository
public interface AlumniRepository extends JpaRepository<Alumni, Long> {

    List<Alumni> findByAlumniSearchId(Long searchId);
    
    boolean existsByLinkedinProfileUrl(String linkedinProfileUrl);

}
