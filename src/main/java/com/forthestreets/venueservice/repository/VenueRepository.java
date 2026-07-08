package com.forthestreets.venueservice.repository;

import com.forthestreets.venueservice.domain.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    /**
     * Finds all venues within a given radius (meters) of a specific latitude/longitude coordinate.
     * ST_DWithin checks if geometries are within a distance.
     * ST_SetSRID/ST_MakePoint constructs a native PostGIS point from inputs.
     */
    @Query(value = """
        SELECT v.* FROM venues v 
        WHERE ST_DWithin(
            v.location, 
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326), 
            :radiusInMeters
        )
        """, nativeQuery = true)
    List<Venue> findVenuesNearby(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusInMeters") double radiusInMeters
    );
}