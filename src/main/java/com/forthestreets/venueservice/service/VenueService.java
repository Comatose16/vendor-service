package com.forthestreets.venueservice.service;

import com.forthestreets.venueservice.dto.VenueRequest;
import com.forthestreets.venueservice.dto.VenueResponse;
import java.util.List;

public interface VenueService {
    VenueResponse createVenue(VenueRequest request);
    VenueResponse getVenueById(Long id);
    List<VenueResponse> getVenuesNearby(double latitude, double longitude, double radiusInMeters);
    VenueResponse updateVenue(Long id, VenueRequest request);
    void deleteVenue(Long id);
}