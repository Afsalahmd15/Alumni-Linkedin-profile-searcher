
package com.example.alumni.service;

import org.springframework.stereotype.Service;

import com.example.alumni.model.University;
import com.example.alumni.repository.UniversityRepository;

import tools.jackson.databind.JsonNode;

@Service
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final BridglyService bridglyService;

    public UniversityService(
            UniversityRepository universityRepository,
            BridglyService bridglyService) {

        this.universityRepository = universityRepository;
        this.bridglyService = bridglyService;
    }

    public University findOrCreateUniversity(String universityName) {

        // 1. Check PostgreSQL first
        return universityRepository
                .findByNameIgnoreCase(universityName)
                .orElseGet(() -> discoverUniversity(universityName));
    }

    private University discoverUniversity(String universityName) {

        // 2. Search using Bridgly
        JsonNode response = bridglyService.searchSchools(universityName);

        JsonNode schools = response.path("data").path("schools");

        if (!schools.isArray() || schools.isEmpty()) {
            throw new RuntimeException(
                    "University not found: " + universityName);
        }

        // 3. Select the matching university
        JsonNode selectedSchool = null;

        for (JsonNode school : schools) {
            String name = school.path("name").asText("");

            if (name.equalsIgnoreCase(universityName)) {
                selectedSchool = school;
                break;
            }
        }

        if (selectedSchool == null) {
            throw new RuntimeException(
                    "No exact university match found. Please verify the university name.");
        }

        // 4. Convert API response into entity
        University university = new University();

        university.setName(selectedSchool.path("name").asText());
        university.setSchoolId(selectedSchool.path("schoolId").asText());
        university.setSchoolUrn(selectedSchool.path("schoolUrn").asText(null));
        university.setSchoolUrl(selectedSchool.path("schoolUrl").asText(null));
        university.setLocation(selectedSchool.path("location").asText(null));

        // 5. Save in PostgreSQL
        return universityRepository.save(university);
    }
}
