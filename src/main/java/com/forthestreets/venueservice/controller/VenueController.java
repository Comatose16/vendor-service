package com.forthestreets.venueservice.controller;

import com.forthestreets.venueservice.dto.VenueRequest;
import com.forthestreets.venueservice.dto.VenueResponse;
import com.forthestreets.venueservice.service.VenueService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.forthestreets.venueservice.util.GeometryUtils.milesToMeters;

@RestController
@RequestMapping("/api/v1/venues")
public class VenueController {

    private static final Logger log = LoggerFactory.getLogger(VenueController.class);

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    /**
     * Creates a new venue on the map.
     * Returns HTTP 201 (Created) upon successful completion.
     */
    @PostMapping
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest request) {
        log.info("Creating venue '{}' at coordinates: ({}, {})",
                request.name(), request.latitude(), request.longitude());

        VenueResponse response = venueService.createVenue(request);

        log.info("Successfully created new venue: '{}' with assigned ID: {}", response.name(), response.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 🔍 Fetches details of a specific venue by its database ID.
     * Logged at DEBUG level to keep production consoles silent under high-traffic spikes.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long id) {
        log.debug("Fetching details for venue ID: {}", id);

        VenueResponse response = venueService.getVenueById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for venues closest to a specific location on the map.
     * Integrates with our PostGIS spatial query engine.
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<VenueResponse>> getVenuesNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusInMiles) {

        log.debug("Scanning nearby venues around coordinates: ({}, {}) within radius: {} miles",
                latitude, longitude, radiusInMiles);

        double radiusInMeters = milesToMeters(radiusInMiles);

        List<VenueResponse> response = venueService.getVenuesNearby(latitude, longitude, radiusInMeters);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing venue's properties.
     * Triggers dynamic dirty checking on exit from the Service boundary.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VenueResponse> updateVenue(
            @PathVariable Long id,
            @RequestBody VenueRequest request) {

        log.info("Updating venue ID: {} to name: '{}'", id, request.name());

        VenueResponse response = venueService.updateVenue(id, request);

        log.info("Successfully updated venue ID: {} to name: '{}'", id, request.name());

        return ResponseEntity.ok(response);
    }

    /**
     * Permanently deletes a venue and all associated event sub-dependencies.
     * Returns a crisp, standard HTTP 204 (No Content).
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVenue(@PathVariable Long id) {
        log.info("Permanently removing venue ID: {}", id);

        venueService.deleteVenue(id);

        log.info("Successfully removed venue ID: {}", id);

    }
}