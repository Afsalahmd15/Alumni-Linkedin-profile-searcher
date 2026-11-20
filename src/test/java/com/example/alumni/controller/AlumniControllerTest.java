
package com.example.alumni.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.alumni.dto.AlumniResponse;
import com.example.alumni.service.AlumniService;

import tools.jackson.databind.ObjectMapper;

class AlumniControllerTest {

    private MockMvc mockMvc;

    private AlumniService alumniService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {

        alumniService = mock(AlumniService.class);

        AlumniController controller =
                new AlumniController(alumniService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldReturnAlumniSuccessfully() throws Exception {

        AlumniResponse alumni = new AlumniResponse(
                "Afsal",
                "Java Developer",
                "Vidya Academy",
                "Thrissur",
                "Java Developer",
                2025
        );

        when(alumniService.searchAlumni(any()))
                .thenReturn(List.of(alumni));

        String request = """
            {
                "university": "Vidya Academy",
                "designation": "Java Developer",
                "passoutYear": 2025
            }
            """;

        mockMvc.perform(post("/api/alumni/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Afsal"))
                .andExpect(jsonPath("$[0].currentRole")
                        .value("Java Developer"));

        verify(alumniService).searchAlumni(any());
    }

    @Test
    void shouldReturnEmptyListWhenNoAlumniFound() throws Exception {

        when(alumniService.searchAlumni(any()))
                .thenReturn(List.of());

        String request = """
            {
                "university": "Vidya Academy",
                "designation": "Java Developer",
                "passoutYear": 2025
            }
            """;

        mockMvc.perform(post("/api/alumni/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnBadRequestWhenUniversityIsMissing()
            throws Exception {

        String request = """
            {
                "designation": "Java Developer",
                "passoutYear": 2025
            }
            """;

        mockMvc.perform(post("/api/alumni/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alumniService);
    }

    @Test
    void shouldReturnBadRequestWhenDesignationIsMissing()
            throws Exception {

        String request = """
            {
                "university": "Vidya Academy",
                "passoutYear": 2025
            }
            """;

        mockMvc.perform(post("/api/alumni/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alumniService);
    }
}
