package com.forthestreets.venueservice;

import com.forthestreets.venueservice.domain.Venue;
import com.forthestreets.venueservice.repository.VenueRepository;
import com.forthestreets.venueservice.util.GeometryUtils;
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
import static org.assertj.core.api.Assertions.within;

@Transactional // Automatically rolls back transaction after each test, keeping the database clean
@DisplayName("Venue Repository Integration Tests (PostGIS Container)")
class VenueRepositoryIntegrationTest extends BaseIntegrationTest {

    public static final double INTUIT_DOME_LAT = 33.9456;

    public static final double INTUIT_DOME_LNG = -118.3418;

    public static final double THREE_WEAVERS_LAT = 33.9678;

    public static final double THREE_WEAVERS_LNG = -118.3734;

    @Autowired
    private VenueRepository venueRepository;

    // JTS Geometry Factory utilizing World Geodetic System (SRID 4326) coordinates
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        venueRepository.deleteAllInBatch();

        // 1. Create and persist Three Weavers Brewing (Inside radius: ~2.3 miles / ~3701.49m)
        Venue threeWeavers = new Venue();
        threeWeavers.setName("Three Weavers Brewing Company");
        threeWeavers.setAddress("1005 W Manchester Blvd, Inglewood, CA 90301");
        threeWeavers.setLocation(createPoint(THREE_WEAVERS_LAT, THREE_WEAVERS_LNG));
        venueRepository.save(threeWeavers);

        // 2. Create and persist The Miracle Theater (Inside radius: ~1.9 miles / ~3057.75m)
        Venue miracleTheater = new Venue();
        miracleTheater.setName("The Miracle Theater");
        miracleTheater.setAddress("226 S Market St, Inglewood, CA 90301");
        miracleTheater.setLocation(createPoint(33.9617, -118.3533));
        venueRepository.save(miracleTheater);

        // 3. Create and persist Dockweiler Beach (Outside radius: ~ 5.84 miles / ~9398.569m)
        Venue dockweilerBeach = new Venue();
        dockweilerBeach.setName("Dockweiler Beach");
        dockweilerBeach.setAddress("12001 Vista Del Mar, Playa Del Rey, CA 90293");
        dockweilerBeach.setLocation(createPoint(33.9366, -118.4431));
        venueRepository.save(dockweilerBeach);
    }

    @Test
    @DisplayName("🌐 Precision Check: Validate offline mathematical distances against WGS84 standards")
    void shouldVerifyExactGeodeticCoordinatesWithJsr385() {

        double distanceInMiles = GeometryUtils.calculateHaversineDistance(
                INTUIT_DOME_LAT, INTUIT_DOME_LNG,
                THREE_WEAVERS_LAT, THREE_WEAVERS_LNG
        ).to(GeometryUtils.MILE).getValue().doubleValue();

        // Target check: ~2.37 miles. We assert with a precision margin of 0.05 miles
        assertThat(distanceInMiles)
                .as("Validate mathematical distance matches offline geographical expectations")
                .isCloseTo(2.37, within(0.05));
    }

    /**
     * Three Weavers is 2.3 miles (Included)
     * Miracle Theater is 1.9 miles (Included)
     * Dockweiler Beach is 5.84 miles (Excluded)
     */
    @Test
    @DisplayName("ST_DWithin Integration: Should find closest venues within narrow 2.5 mile radius")
    void shouldFindVenuesWithinNarrowMileRadius() {
        // 2.5 miles converted to meters = ~4023.35 meters
        double radiusInMetersFor2Point5Miles = milesToMeters(2.5);

        List<Venue> nearbyVenues = venueRepository.findVenuesNearby(
                INTUIT_DOME_LAT,
                INTUIT_DOME_LNG,
                radiusInMetersFor2Point5Miles
        );

        assertThat(nearbyVenues)
                .hasSize(2)
                .extracting(Venue::getName)
                .containsExactlyInAnyOrder("Three Weavers Brewing Company", "The Miracle Theater");
    }

    /*
     * Under 5 miles, both Three Weavers (2.3 mi) and Miracle Theater (1.9 mi) are caught.
     * Dockweiler Beach (5.84 mi) remains safely filtered out.
    */
    @Test
    @DisplayName("ST_DWithin Integration: Should capture outer venues within a broader 5.0 mile radius")
    void shouldFindVenuesWithinBiggerMileRadius() {

        // 5.0 miles converted to meters = ~8046.7 meters
        double radiusInMetersFor5Miles = milesToMeters(5.0);

        List<Venue> nearbyVenues = venueRepository.findVenuesNearby(
                INTUIT_DOME_LAT,
                INTUIT_DOME_LNG,
                radiusInMetersFor5Miles
        );

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
