
package com.example.alumni.controller;

import com.example.alumni.service.PhantomBusterService;
import tools.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/phantom")
public class PhantomBusterController {

    private final PhantomBusterService phantomBusterService;

    public PhantomBusterController(
            PhantomBusterService phantomBusterService) {
        this.phantomBusterService = phantomBusterService;
    }

    @PostMapping("/launch")
    public JsonNode launchPhantom() {

        String linkedInSearchUrl =
            "https://www.linkedin.com/search/results/people/?keywords=java%20developer%20&origin=FACETED_SEARCH&schoolFilter=%5B%221576527%22%5D";

        return phantomBusterService.launchPhantom(linkedInSearchUrl);
    }
    @GetMapping("/results/{containerId}")
    public JsonNode fetchResults(
        @PathVariable String containerId
    ){

        return phantomBusterService.fetchResults(containerId);
    }
}
