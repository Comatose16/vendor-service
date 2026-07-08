package com.forthestreets.venueservice.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public final class GeometryUtils {

    // SRID 4326 represents standard spatial coordinates (WGS84 lat/lng)
    private static final int SRID = 4326;
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    private GeometryUtils() {
        // Prevent instantiation of utility class
    }

    /**
     * Converts a raw latitude and longitude into a JTS Point object for database storage.
     */
    public static Point createPoint(double latitude, double longitude) {
        // Crucial: JTS Coordinate takes (x, y) which maps to (longitude, latitude)
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }
}