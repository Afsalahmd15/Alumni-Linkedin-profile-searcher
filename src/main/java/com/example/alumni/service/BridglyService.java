
package com.example.alumni.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Service
public class BridglyService {

    private final RestClient restClient;

    @Value("${bridgly.api.key}")
    private String apiKey;

    public BridglyService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.bridgly.app")
                .build();
    }

    public JsonNode searchSchools(String universityName) {

        Map<String, Object> requestBody = Map.of(
                "keyword", universityName,
                "count", 5
        );

        return restClient.post()
                .uri("/linkedin/search/schools")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);
    }
}
