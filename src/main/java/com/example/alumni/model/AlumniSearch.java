package com.example.alumni.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="alumni_search")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class AlumniSearch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String university;

    private String designation;

    private Integer passoutYear;

    private LocalDateTime searchedAt;
    
    private String status;
    
    @OneToMany(mappedBy = "alumniSearch", cascade = CascadeType.ALL)
    private List<Alumni> alumni;
    
}
