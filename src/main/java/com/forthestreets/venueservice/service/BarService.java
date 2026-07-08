package com.forthestreets.venueservice.service;

import com.forthestreets.venueservice.domain.Venue;
import com.forthestreets.venueservice.dto.VenueRequest;
import com.forthestreets.venueservice.dto.VenueResponse;
import com.forthestreets.venueservice.repository.VenueRepository;
import com.forthestreets.venueservice.util.GeometryUtils;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BarService implements VenueService {

    private final VenueRepository venueRepository;

    public BarService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        Point spatialPoint = GeometryUtils.createPoint(request.latitude(), request.longitude());
        Venue venue = new Venue(request.name(), request.address(), spatialPoint);
        Venue savedVenue = venueRepository.save(venue);
        return mapToResponse(savedVenue);
    }

    @Override
    public VenueResponse getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bar not found with id: " + id));
        return mapToResponse(venue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueResponse> getVenuesNearby(double latitude, double longitude, double radiusInMeters) {
        return venueRepository.findVenuesNearby(latitude, longitude, radiusInMeters)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VenueResponse updateVenue(Long id, VenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bar not found with id: " + id));

        Point spatialPoint = GeometryUtils.createPoint(request.latitude(), request.longitude());

        venue.setName(request.name());
        venue.setAddress(request.address());
        venue.setLocation(spatialPoint);

        // No explicit repository.save() needed here because of @Transactional state management!
        return mapToResponse(venue);
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        if (!venueRepository.existsById(id)) {
            throw new RuntimeException("Bar not found with id: " + id);
        }
        venueRepository.deleteById(id);
    }

    private VenueResponse mapToResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddress(),
                venue.getLocation().getY(),
                venue.getLocation().getX()
        );
    }
}