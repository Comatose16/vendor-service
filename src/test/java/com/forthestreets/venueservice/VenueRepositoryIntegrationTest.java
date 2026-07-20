package com.forthestreets.venueservice;

import com.forthestreets.venueservice.domain.Venue;
import com.forthestreets.venueservice.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.forthestreets.venueservice.util.GeometryUtils.milesToMeters;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional // Automatically rolls back transaction after each test, keeping the database clean
@DisplayName("Venue Repository Integration Tests (PostGIS Container)")
class VenueRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private VenueRepository venueRepository;

    // JTS Geometry Factory utilizing World Geodetic System (SRID 4326) coordinates
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        venueRepository.deleteAll();

        // 1. Create and persist Three Weavers Brewing (Inside radius: ~2.1 miles / ~3380m)
        Venue threeWeavers = new Venue();
        threeWeavers.setName("Three Weavers Brewing Company");
        threeWeavers.setAddress("1005 W Manchester Blvd, Inglewood, CA 90301");
        threeWeavers.setLocation(createPoint(33.9678, -118.3734));
        venueRepository.save(threeWeavers);

        // 2. Create and persist The Miracle Theater (Inside radius: ~2.8 miles / ~4500m)
        Venue miracleTheater = new Venue();
        miracleTheater.setName("The Miracle Theater");
        miracleTheater.setAddress("226 S Market St, Inglewood, CA 90301");
        miracleTheater.setLocation(createPoint(33.9617, -118.3533));
        venueRepository.save(miracleTheater);

        // 3. Create and persist Dockweiler Beach (Outside radius: ~7.8 miles / ~12500m)
        Venue dockweilerBeach = new Venue();
        dockweilerBeach.setName("Dockweiler Beach");
        dockweilerBeach.setAddress("12001 Vista Del Mar, Playa Del Rey, CA 90293");
        dockweilerBeach.setLocation(createPoint(33.9366, -118.4431));
        venueRepository.save(dockweilerBeach);
    }

    @Test
    @DisplayName("ST_DWithin Integration: Should find closest venues within narrow 2.5 mile radius")
    void shouldFindVenuesWithinNarrowMileRadius() {
        // We stand at the Intuit Dome (33.9456, -118.3418)
        double intuitDomeLat = 33.9456;
        double intuitDomeLng = -118.3418;

        // 2.5 miles converted to meters = ~4023.35 meters
        double radiusInMetersFor2Point5Miles = milesToMeters(2.5);

        List<Venue> nearbyVenues = venueRepository.findVenuesNearby(
                intuitDomeLat,
                intuitDomeLng,
                radiusInMetersFor2Point5Miles
        );

        // Three Weavers is 2.1 miles (Included)
        // Miracle Theater is 2.8 miles (Excluded)
        // Dockweiler Beach is 7.8 miles (Excluded)
        assertThat(nearbyVenues)
                .hasSize(1)
                .extracting(Venue::getName)
                .containsExactly("Three Weavers Brewing Company");
    }

    @Test
    @DisplayName("ST_DWithin Integration: Should capture outer venues within a broader 5.0 mile radius")
    void shouldFindVenuesWithinBiggerMileRadius() {
        double intuitDomeLat = 33.9456;
        double intuitDomeLng = -118.3418;

        // 5.0 miles converted to meters = ~8046.7 meters
        double radiusInMetersFor5Miles = milesToMeters(5.0);

        List<Venue> nearbyVenues = venueRepository.findVenuesNearby(
                intuitDomeLat,
                intuitDomeLng,
                radiusInMetersFor5Miles
        );

        // Under 5 miles, both Three Weavers (2.1 mi) and Miracle Theater (2.8 mi) are caught.
        // Dockweiler Beach (7.8 mi) remains safely filtered out.
        assertThat(nearbyVenues)
                .hasSize(2)
                .extracting(Venue::getName)
                .containsExactlyInAnyOrder("Three Weavers Brewing Company", "The Miracle Theater");
    }

    /**
     * Helper to wrap raw coordinates into a standardized geographic JTS Point.
     * Note: JTS coordinates accept inputs in (X, Y) order which equates to (Longitude, Latitude).
     */
    private Point createPoint(double latitude, double longitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326);
        return point;
    }
}
