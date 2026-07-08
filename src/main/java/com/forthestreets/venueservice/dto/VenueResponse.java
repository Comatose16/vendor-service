package com.forthestreets.venueservice.dto;

import java.util.List;

public record VenueResponse(
        Long id,
        String name,
        String address,
        double latitude,
        double longitude
) {}