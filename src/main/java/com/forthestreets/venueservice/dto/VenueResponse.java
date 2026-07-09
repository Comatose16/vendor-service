package com.forthestreets.venueservice.dto;

public record VenueResponse(
        Long id,
        String name,
        String address,
        double latitude,
        double longitude
) {}