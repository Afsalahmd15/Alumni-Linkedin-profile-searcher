
package com.example.alumni.dto;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class AlumniSearchRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void shouldAcceptValidRequest() {

        AlumniSearchRequest request = new AlumniSearchRequest(
                "Vidya Academy",
                "Java Developer",
                2025
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectBlankUniversity() {

        AlumniSearchRequest request = new AlumniSearchRequest(
                "",
                "Java Developer",
                2025
        );

        assertTrue(
                validator.validate(request).stream()
                        .anyMatch(error -> error.getMessage()
                                .equals("University name is required"))
        );
    }

    @Test
    void shouldRejectBlankDesignation() {

        AlumniSearchRequest request = new AlumniSearchRequest(
                "Vidya Academy",
                " ",
                2025
        );

        assertTrue(
                validator.validate(request).stream()
                        .anyMatch(error -> error.getMessage()
                                .equals("Designation is required"))
        );
    }

    @Test
    void shouldAcceptNullPassoutYear() {

        AlumniSearchRequest request = new AlumniSearchRequest(
                "Vidya Academy",
                "Java Developer",
                null
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void shouldRejectPassoutYearBelow1900() {

        AlumniSearchRequest request = new AlumniSearchRequest(
                "Vidya Academy",
                "Java Developer",
                1899
        );

        assertTrue(
                validator.validate(request).stream()
                        .anyMatch(error -> error.getMessage()
                                .equals("Invalid passout year"))
        );
    }
}
