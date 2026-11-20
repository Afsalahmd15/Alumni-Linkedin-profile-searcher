
package com.example.alumni.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "alumni",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"linkedin_profile_url", "search_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alumni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "designation")
    private String currentRole;

    private String university;

    private String location;

    @Column(length = 1000)
    private String linkedinHeadline;

    private Integer passoutYear;

    private String linkedinProfileUrl;

    @ManyToOne
    @JoinColumn(name = "search_id")
    private AlumniSearch alumniSearch;
}
