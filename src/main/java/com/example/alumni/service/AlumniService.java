package com.example.alumni.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import com.example.alumni.dto.AlumniResponse;
import com.example.alumni.dto.AlumniSearchRequest;
import com.example.alumni.model.Alumni;
import com.example.alumni.model.AlumniSearch;
import com.example.alumni.model.University;
import com.example.alumni.repository.AlumniRepository;
import com.example.alumni.repository.AlumniSearchRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
@Service
public class AlumniService {

    private final AlumniRepository alumniRepository;
    private final AlumniSearchRepository alumniSearchRepository;
    
    private final PhantomBusterService phantomBusterService;

    private final UniversityService universityService;
    private final ObjectMapper objectMapper;
    public AlumniService(AlumniRepository alumniRepository,
        AlumniSearchRepository alumniSearchRepository,
         PhantomBusterService phantomBusterService,
        UniversityService universityService,
        ObjectMapper objectMapper) {
        
        this.alumniRepository = alumniRepository;
        this.alumniSearchRepository = alumniSearchRepository;
        this.phantomBusterService = phantomBusterService;
        this.universityService = universityService;
        this.objectMapper = objectMapper;
    }

    
@Transactional
public List<AlumniResponse> searchAlumni(AlumniSearchRequest request) {

    Optional<AlumniSearch> existingSearch =
            alumniSearchRepository
                    .findFirstByUniversityIgnoreCaseAndDesignationIgnoreCaseAndPassoutYear(
                            request.getUniversity(),
                            request.getDesignation(),
                            request.getPassoutYear()
                    );

    if (existingSearch.isPresent()
            && "COMPLETED".equals(existingSearch.get().getStatus())) {

        List<Alumni> alumniList =
                alumniRepository.findByAlumniSearchId(
                        existingSearch.get().getId()
                );

        return alumniList.stream()
                .map(this::mapToResponse)
                .toList();
    }
    if (existingSearch.isPresent()
        && "IN_PROGRESS".equals(existingSearch.get().getStatus())) {

    throw new IllegalStateException(
            "This alumni search is already in progress."
    );
}
    AlumniSearch newSearch = new AlumniSearch();

    newSearch.setUniversity(request.getUniversity());
    newSearch.setDesignation(request.getDesignation());
    newSearch.setPassoutYear(request.getPassoutYear());
    newSearch.setSearchedAt(LocalDateTime.now());
    newSearch.setStatus("IN_PROGRESS");

    newSearch = alumniSearchRepository.save(newSearch);

    try {

        String linkedInUrl = buildLinkedInSearchUrl(request);

        JsonNode launchResponse =
                phantomBusterService.launchPhantom(linkedInUrl);

        String containerId =
                launchResponse.path("containerId").asText(null);

        if (containerId == null || containerId.isBlank()) {
            throw new RuntimeException(
                    "PhantomBuster did not return a container ID"
            );
        }

        System.out.println("PhantomBuster Container ID: " + containerId);
        System.out.println("Launch response: " + launchResponse);
        System.out.println("Container ID: " + containerId);
        JsonNode resultObject = waitForPhantom(containerId);

        List<Alumni> alumniList =
                processPhantomResults(resultObject, newSearch);


        List<Alumni> savedAlumni =
                alumniRepository.saveAll(alumniList);

        newSearch.setStatus("COMPLETED");
        alumniSearchRepository.save(newSearch);

        return savedAlumni.stream()
                .map(this::mapToResponse)
                .toList();

    } catch (InterruptedException e) {

        Thread.currentThread().interrupt();
        newSearch.setStatus("FAILED");
        alumniSearchRepository.save(newSearch);

        throw new RuntimeException(
                "PhantomBuster execution interrupted", e
        );

    } catch (Exception e) {

        newSearch.setStatus("FAILED");
        alumniSearchRepository.save(newSearch);

        throw new RuntimeException(
                "Alumni search failed: " + e.getMessage(), e
        );
    }
}


    private AlumniResponse mapToResponse(Alumni alumni) {

    return new AlumniResponse(
            alumni.getName(),
            alumni.getCurrentRole(),
            alumni.getUniversity(),
            alumni.getLocation(),
            alumni.getLinkedinHeadline(),
            alumni.getPassoutYear()
    );
    }

    private Alumni mapToAlumni(JsonNode profile, AlumniSearch search) {

    Alumni alumni = new Alumni();

    alumni.setName(profile.path("fullName").asText(null));

    alumni.setCurrentRole(
        profile.path("jobTitle").asText(null)
    );

    alumni.setUniversity(
        profile.path("school").asText(null)
    );

    alumni.setLocation(
        profile.path("location").asText(null)
    );

    alumni.setLinkedinHeadline(
        profile.path("headline").asText(null)
    );

    alumni.setLinkedinProfileUrl(
        profile.path("profileUrl").asText(null)
    );

    alumni.setPassoutYear(
        extractPassoutYear(
            profile.path("schoolDateRange").asText("")
        )
    );

    alumni.setAlumniSearch(search);

    return alumni;
} 

   private Integer extractPassoutYear(String dateRange) {

    if (dateRange == null || dateRange.isBlank()) {
        return null;
    }

    Matcher matcher = Pattern.compile("(19|20)\\d{2}")
            .matcher(dateRange);

    Integer lastYear = null;

    while (matcher.find()) {
        lastYear = Integer.parseInt(matcher.group());
    }

    return lastYear;
}

    
private List<Alumni> processPhantomResults(
        JsonNode profiles,
        AlumniSearch search) {

    if (!profiles.isArray()) {
        throw new IllegalArgumentException(
                "Expected an array of alumni profiles"
        );
    }

    List<Alumni> alumniList = new java.util.ArrayList<>();

    for (JsonNode profile : profiles) {

        String name = profile.path("fullName").asText(null);
        String university = profile.path("school").asText(null);
        String profileUrl = profile.path("profileUrl").asText(null);

        // 1. Validate required fields
        if (name == null || name.isBlank()
                || profileUrl == null || profileUrl.isBlank()) {
            continue;
        }

        // 2. Match university
        if (!matchesUniversity(university, search.getUniversity())) {
            continue;
        }

        // 3. Match designation
        if (!matchesDesignation(profile, search.getDesignation())) {
            continue;
        }

        // 4. Match pass-out year only when supplied
        if (search.getPassoutYear() != null) {

            Integer year = extractPassoutYear(
                    profile.path("schoolDateRange").asText("")
            );

            if (!search.getPassoutYear().equals(year)) {
                continue;
            }
        }

        alumniList.add(mapToAlumni(profile, search));
    }

    return alumniList;
}

private boolean matchesUniversity(
        String actualUniversity,
        String requestedUniversity) {

    if (actualUniversity == null || requestedUniversity == null) {
        return false;
    }

    String actual = actualUniversity.toLowerCase()
            .replace("&", "and")
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();

    String requested = requestedUniversity.toLowerCase()
            .replace("&", "and")
            .replaceAll("[^a-z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();

    return actual.equals(requested)
            || actual.contains(requested)
            || requested.contains(actual);
}
private boolean matchesDesignation(
        JsonNode profile,
        String designation) {

    String jobTitle = profile.path("jobTitle").asText("");
    String headline = profile.path("headline").asText("");

    String searchTerm = designation.toLowerCase().trim();

    return jobTitle.toLowerCase().contains(searchTerm)
            || headline.toLowerCase().contains(searchTerm);
}

    

private JsonNode waitForPhantom(String containerId)
        throws InterruptedException, tools.jackson.core.JacksonException {

    for (int attempt = 0; attempt < 180; attempt++) {

        JsonNode container;

        try {
            container = phantomBusterService.fetchContainer(containerId);
        } catch (ResourceAccessException e) {
            System.out.println("Temporary connection issue. Retrying...");
            Thread.sleep(5000);
            continue;
        }

        String status = container.path("status").asText();

        System.out.println("Attempt: " + (attempt + 1));
        System.out.println("Container status: " + status);

        if ("finished".equalsIgnoreCase(status)) {

            String output1 = container.path("output").asText("");

if (output1.toLowerCase().contains("monthly search limits reached")) {
    throw new RuntimeException(
            "Monthly search limits reached for the connected LinkedIn account. Please try again after the allowance resets."
    );
}
            if (container.path("exitCode").asInt(-1) != 0) {
                throw new RuntimeException(
                        "PhantomBuster execution failed: "
                                + container.path("output").asText()
                );
            }

            JsonNode rawResultObject = container.path("resultObject");
            if (rawResultObject.isMissingNode()
        || rawResultObject.isNull()) {

    System.out.println("Trying fetchResults endpoint...");

    JsonNode fetchedResult =
            phantomBusterService.fetchResults(containerId);
            System.out.println("FETCH RESULTS RESPONSE: " + fetchedResult);

    if (!fetchedResult.isMissingNode()
            && !fetchedResult.isNull()) {

        System.out.println("Fetched result object successfully");

        return normalizeProfiles(fetchedResult);
    }
}

            if (rawResultObject.isMissingNode()
                    || rawResultObject.isNull()) {

                // Fallback: look for JSON URL in PhantomBuster output
                String output = container.path("output").asText();

                java.util.regex.Matcher matcher =
                        java.util.regex.Pattern
                                .compile("JSON saved at (https://\\S+)")
                                .matcher(output);

                if (matcher.find()) {
                    String jsonUrl = matcher.group(1);

                    System.out.println("JSON URL found in output: " + jsonUrl);

                    JsonNode downloadedProfiles =
                            phantomBusterService.downloadJsonFile(jsonUrl);

                    return normalizeProfiles(downloadedProfiles);
                }

                throw new RuntimeException(
                        "PhantomBuster returned neither resultObject nor JSON URL"
                );
            }

            // Parse resultObject if it is returned as a JSON string
            JsonNode result = rawResultObject;

            if (rawResultObject.isTextual()) {
                result = objectMapper.readTree(rawResultObject.asText());
            }

            // Case 1: Actual profiles are returned directly as an array
            if (result.isArray()) {
                System.out.println("Received profiles directly from PhantomBuster");
                return result;
            }

            // Case 2: Object contains a data array
            if (result.has("data") && result.path("data").isArray()) {
                System.out.println("Received profiles inside data object");
                return result.path("data");
            }

            // Case 3: Object contains a JSON download URL
            String jsonUrl = result.path("jsonUrl").asText(null);

            if (jsonUrl != null && !jsonUrl.isBlank()) {

                System.out.println("Downloading profiles from: " + jsonUrl);

                JsonNode downloadedProfiles =
                        phantomBusterService.downloadJsonFile(jsonUrl);

                return normalizeProfiles(downloadedProfiles);
            }

            throw new RuntimeException(
                    "Unexpected PhantomBuster result format: " + result
            );
        }

        if ("error".equalsIgnoreCase(status)) {
            throw new RuntimeException(
                    "PhantomBuster execution failed: "
                            + container.path("output").asText()
            );
        }

        Thread.sleep(5000);
    }

    throw new RuntimeException(
            "PhantomBuster execution timed out after 15 minutes"
    );
}

private JsonNode normalizeProfiles(JsonNode result) {

    // Direct array of profiles
    if (result.isArray()) {
        return result;
    }

    // Object containing profiles in data
    if (result.has("data") && result.path("data").isArray()) {
        return result.path("data");
    }

    throw new IllegalArgumentException(
            "Downloaded JSON does not contain a profile array"
    );
}

  private String buildLinkedInSearchUrl(AlumniSearchRequest request) {

    // Find university in PostgreSQL or discover it using Bridgly
    University university = universityService.findOrCreateUniversity(
            request.getUniversity()
    );

    String keywords = java.net.URLEncoder.encode(
            request.getDesignation(),
            java.nio.charset.StandardCharsets.UTF_8
    );

    String schoolId = university.getSchoolId();

    return "https://www.linkedin.com/search/results/people/"
            + "?keywords=" + keywords
            + "&origin=FACETED_SEARCH"
            + "&schoolFilter=%5B%22" + schoolId + "%22%5D";
    }

    public List<AlumniResponse> getAllAlumni() {

    return alumniRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .toList();
}
}
