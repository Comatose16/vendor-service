package com.forthestreets.venueservice.exception;

public class VenueNotFoundException extends VenueException {
    public VenueNotFoundException(Long id) {
        super(String.format("Venue with ID %d could not be found.", id));
    }

    public VenueNotFoundException(String message) {
        super(message);
    }
}
