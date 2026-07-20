package com.forthestreets.venueservice.controller;

import com.forthestreets.venueservice.dto.VenueRequest;
import com.forthestreets.venueservice.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VenueController.class)
@DisplayName("🎯 Venue HTTP Request Validation Tests")
class VenueControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VenueService venueService;

    @Nested
    @DisplayName("Blank & Missing Field Constraints")
    class BlankFieldTests {

        @Test
        @DisplayName("Validation Failure: Empty name and blank address should reject request")
        void shouldRejectEmptyNameAndAddress() throws Exception {
            VenueRequest badRequest = new VenueRequest("", "   ", 33.9456, -118.3418);

            MvcResult result = mockMvc.perform(post("/api/v1/venues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            Map<String, Object> responseBody = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            assertThat(responseBody)
                    .isNotNull()
                    .containsEntry("status", 400)
                    .containsEntry("error", "Bad Request")
                    .containsEntry("path", "/api/v1/venues");

            @SuppressWarnings("unchecked")
            Map<String, String> validationErrors = (Map<String, String>) responseBody.get("errors");

            assertThat(validationErrors)
                    .isNotNull()
                    .hasSize(2)
                    .containsEntry("name", "Venue name is required and cannot be blank")
                    .containsEntry("address", "Address is required and cannot be blank");

            verifyNoInteractions(venueService);
        }
    }

    @Nested
    @DisplayName("🌐 Spatial Boundary Constraints")
    class SpatialBoundaryTests {

        @Test
        @DisplayName("Validation Failure: Out-of-bounds coordinates should fail boundary check")
        void shouldRejectInvalidCoordinates() throws Exception {
            VenueRequest badRequest = new VenueRequest("Out of Bounds Club", "Space", 95.5, -200.0);

            MvcResult result = mockMvc.perform(post("/api/v1/venues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            Map<String, Object> responseBody = objectMapper.readValue(jsonResponse, new TypeReference<>() {
            });

            @SuppressWarnings("unchecked")
            Map<String, String> validationErrors = (Map<String, String>) responseBody.get("errors");

            assertThat(validationErrors)
                    .isNotNull()
                    .hasSize(2)
                    .containsEntry("latitude", "Latitude must be less than or equal to 90")
                    .containsEntry("longitude", "Longitude must be greater than or equal to -180");

            verifyNoInteractions(venueService);
        }

        @Test
        @DisplayName("Validation Failure: Explicit Null coordinates should fail null check")
        void shouldRejectNullCoordinates() throws Exception {

            VenueRequest badRequest = new VenueRequest("The Null Cafe", "Downtown Inglewood", null, null);
;
            MvcResult result = mockMvc.perform(post("/api/v1/venues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badRequest)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            Map<String, Object> responseBody = objectMapper.readValue(jsonResponse, new TypeReference<>() {
            });

            @SuppressWarnings("unchecked")
            Map<String, String> validationErrors = (Map<String, String>) responseBody.get("errors");

            assertThat(validationErrors)
                    .isNotNull()
                    .hasSize(2)
                    .containsEntry("latitude", "Latitude is required")
                    .containsEntry("longitude", "Longitude is required");

            verifyNoInteractions(venueService);
        }

        @Test
        @DisplayName("Validation Failure: Omitted key fields should be caught as null constraint violations")
        void shouldRejectOmittedCoordinates() throws Exception {
            // Creating a map and completely omitting the latitude and longitude keys
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", "The Omitted Space");
            payload.put("address", "123 Main St, Inglewood CA");

            MvcResult result = mockMvc.perform(post("/api/v1/venues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            Map<String, Object> responseBody = objectMapper.readValue(jsonResponse, new TypeReference<>() {
            });

            @SuppressWarnings("unchecked")
            Map<String, String> validationErrors = (Map<String, String>) responseBody.get("errors");

            assertThat(validationErrors)
                    .isNotNull()
                    .hasSize(2)
                    .containsEntry("latitude", "Latitude is required")
                    .containsEntry("longitude", "Longitude is required");

            verifyNoInteractions(venueService);
        }

        @Test
        @DisplayName("Validation Failure: Passing String instead of Double should trigger parsing bad request (No 500 error)")
        void shouldRejectInvalidDataType() throws Exception {
            Map<String, Object> payload = new HashMap<>();
            payload.put("name", "Type Mismatch Lounge");
            payload.put("address", "Inglewood");
            payload.put("latitude", "not-a-number"); // Bad data type
            payload.put("longitude", -118.3533);

            MvcResult result = mockMvc.perform(post("/api/v1/venues")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            Map<String, Object> responseBody = objectMapper.readValue(jsonResponse, new TypeReference<>() {
            });

            assertThat(responseBody)
                    .isNotNull()
                    .containsEntry("status", 400)
                    .containsEntry("error", "Bad Request");

            assertThat(responseBody.get("message").toString())
                    .contains("The request payload is malformed or contains invalid data types");

            verifyNoInteractions(venueService);
        }
    }
}