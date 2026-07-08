
package com.forthestreets.venueservice.dto;

public record VenueRequest(
        String name,
        String address,
        double latitude,
        double longitude
) {}
