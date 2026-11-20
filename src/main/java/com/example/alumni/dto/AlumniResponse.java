package com.example.alumni.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlumniResponse {

    private String name;

    private String currentRole;

    private String university;

    private String location;

    private String linkedinHeadline;

    private Integer passoutYear;
}
