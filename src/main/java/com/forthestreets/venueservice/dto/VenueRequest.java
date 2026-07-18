
package com.forthestreets.venueservice.dto;

import jakarta.validation.constraints.*;

public record VenueRequest(

        @NotBlank(message = "Venue name is required and cannot be blank")
        @Size(max = 255, message = "Venue name cannot exceed 255 characters")
        String name,

        @NotBlank(message = "Address is required and cannot be blank")
        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
        @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
        double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
        @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
        double longitude
) {}
