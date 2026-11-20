
package com.example.alumni.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.alumni.dto.AlumniResponse;
import com.example.alumni.dto.AlumniSearchRequest;
import com.example.alumni.service.AlumniService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/alumni")
@Tag(name = "Alumni", description = "Alumni search and retrieval APIs")
public class AlumniController {

    private final AlumniService alumniService;

    public AlumniController(AlumniService alumniService) {
        this.alumniService = alumniService;
    }

    @PostMapping("/search")
    @Operation(
        summary = "Search alumni",
        description = "Search alumni by university, designation and optional pass-out year"
    )
    public ResponseEntity<List<AlumniResponse>> searchAlumni(
            @Valid @RequestBody AlumniSearchRequest request) {

        List<AlumniResponse> response =
                alumniService.searchAlumni(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(
        summary = "Get all alumni",
        description = "Retrieve all alumni records stored in the database"
    )
    public ResponseEntity<List<AlumniResponse>> getAllAlumni() {

        List<AlumniResponse> response =
                alumniService.getAllAlumni();

        return ResponseEntity.ok(response);
    }
}
