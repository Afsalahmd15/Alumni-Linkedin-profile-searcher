
package com.example.alumni.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class AlumniResponseTest {

    @Test
    void shouldCreateAlumniResponseSuccessfully() {

        AlumniResponse response = new AlumniResponse(
                "Afsal",
                "Java Developer",
                "Vidya Academy",
                "Thrissur",
                "Java Developer | Spring Boot",
                2025
        );

        assertEquals("Afsal", response.getName());
        assertEquals("Java Developer", response.getCurrentRole());
        assertEquals("Vidya Academy", response.getUniversity());
        assertEquals("Thrissur", response.getLocation());
        assertEquals(
                "Java Developer | Spring Boot",
                response.getLinkedinHeadline()
        );
        assertEquals(2025, response.getPassoutYear());
    }

    @Test
    void shouldAllowNullPassoutYear() {

        AlumniResponse response = new AlumniResponse(
                "Afsal",
                "Java Developer",
                "Vidya Academy",
                "Thrissur",
                "Java Developer",
                null
        );

        assertNull(response.getPassoutYear());
    }

    @Test
    void shouldUpdateResponseFields() {

        AlumniResponse response = new AlumniResponse();

        response.setName("Afsal");
        response.setCurrentRole("Backend Developer");
        response.setPassoutYear(2025);

        assertEquals("Afsal", response.getName());
        assertEquals("Backend Developer", response.getCurrentRole());
        assertEquals(2025, response.getPassoutYear());
    }
}
