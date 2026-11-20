
package com.example.alumni.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;

@Service
public class PhantomBusterService {

    private final RestClient restClient;

    @Value("${phantombuster.api.key}")
    private String apiKey;

    @Value("${phantombuster.agent.id}")
    private String agentId;

    public PhantomBusterService() {
    this.restClient = RestClient.builder()
            .baseUrl("https://api.phantombuster.com/api/v2")
            .build();
}

    public JsonNode launchPhantom(String linkedInSearchUrl) {

    if (apiKey == null || apiKey.isBlank()) {
        throw new IllegalStateException(
                "PhantomBuster API key is missing"
        );
    }

    System.out.println("PhantomBuster API key loaded: true");
    System.out.println("API key length: " + apiKey.trim().length());

    Map<String, Object> bonusArgument = Map.of(
            "linkedInSearchUrl", linkedInSearchUrl
    );

    Map<String, Object> requestBody = Map.of(
            "id", agentId,
            "bonusArgument", bonusArgument
    );

    return restClient.post()
            .uri("/agents/launch")
            .header("X-Phantombuster-Key", apiKey.trim())
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestBody)
            .retrieve()
            .body(JsonNode.class);
}

    public JsonNode fetchResults(String containerId) {

    return restClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/containers/fetch-result-object")
                    .queryParam("id", containerId)
                    .build())
            .header("X-Phantombuster-Key", apiKey.trim())
            .retrieve()
            .body(JsonNode.class);
    }

    public JsonNode fetchContainer(String containerId) {

    return restClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path("/containers/fetch")
                    .queryParam("id", containerId)
                    .queryParam("withResultObject", "true")
                    .queryParam("withOutput", "true")
                    .build())
            .header("X-Phantombuster-Key", apiKey)
            .retrieve()
            .body(JsonNode.class);
   }
   public JsonNode downloadJsonFile(String jsonUrl) {

    return restClient.get()
            .uri(jsonUrl)
            .retrieve()
            .body(JsonNode.class);
}

}
