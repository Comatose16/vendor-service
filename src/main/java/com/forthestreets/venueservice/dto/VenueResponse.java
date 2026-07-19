package com.forthestreets.venueservice.dto;

public record VenueResponse(
        Long id,
        String name,
        String address,
        Double latitude,
        Double longitude
) {}