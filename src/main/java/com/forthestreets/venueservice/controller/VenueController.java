package com.forthestreets.venueservice.controller;

import com.forthestreets.venueservice.dto.VenueRequest;
import com.forthestreets.venueservice.dto.VenueResponse;
import com.forthestreets.venueservice.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.forthestreets.venueservice.util.GeometryUtils.milesToMeters;

@RestController
@RequestMapping("/api/v1/venues")
@Tag(name = "Venues", description = "Endpoints for managing venues and executing PostGIS spatial radius queries.")
public class VenueController {

    private static final Logger log = LoggerFactory.getLogger(VenueController.class);

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @Operation(summary = "Onboard a new venue", description = "Registers a new venue with PostGIS spatial coordinates (WGS84 SRID 4326).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venue onboarded successfully",
                    content = @Content(schema = @Schema(implementation = VenueResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or coordinates out of range")
    })
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        log.info("Creating venue '{}' at coordinates: ({}, {})",
                request.name(), request.latitude(), request.longitude());

        VenueResponse response = venueService.createVenue(request);

        log.info("Successfully created new venue: '{}' with assigned ID: {}", response.name(), response.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get venue by ID", description = "Fetches detailed metadata for a specific venue.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venue found"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long id) {
        log.debug("Fetching details for venue ID: {}", id);

        VenueResponse response = venueService.getVenueById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/nearby")
    @Operation(summary = "Perform spatial sweep for nearby venues",
            description = "Executes native PostGIS ST_DWithin geography query to find active venues within a given radius.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of venues retrieved within radius")
    })
    public ResponseEntity<List<VenueResponse>> getVenuesNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") @Min(1) @Max(100) double radiusInMiles) {

        log.debug("Scanning nearby venues around coordinates: ({}, {}) within radius: {} miles",
                latitude, longitude, radiusInMiles);

        double radiusInMeters = milesToMeters(radiusInMiles);

        List<VenueResponse> response = venueService.getVenuesNearby(latitude, longitude, radiusInMeters);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update venue details", description = "Updates an existing venue name, address, or location coordinates.")
    public ResponseEntity<VenueResponse> updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody VenueRequest request) {

        log.info("Updating venue ID: {} to name: '{}'", id, request.name());

        VenueResponse response = venueService.updateVenue(id, request);

        log.info("Successfully updated venue ID: {} to name: '{}'", id, request.name());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete venue", description = "Removes a venue and its cascaded events from the registry.")
    public void deleteVenue(@PathVariable Long id) {
        log.info("Permanently removing venue ID: {}", id);

        venueService.deleteVenue(id);

        log.info("Successfully removed venue ID: {}", id);

    }
}