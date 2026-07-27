package com.forthestreets.venueservice.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import tech.units.indriya.quantity.Quantities;
import tech.units.indriya.unit.Units;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

public final class GeometryUtils {

    // SRID 4326 represents standard spatial coordinates (WGS84 lat/lng)
    private static final int SRID = 4326;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), SRID);

    public static final Unit<Length> MILE = Units.METRE.multiply(1609.344).asType(Length.class);

    private static final double EARTH_RADIUS_METERS = 6371000.0;

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

    /**
     *Converts a length measurement from Miles to Meters.
     *Uses type-safe JSR 385 conversions instead of inline hardcoded magic multipliers.
     *
     *@param miles The distance value in US Customary Miles
     * @return The mathematically converted distance in SI Meters
     */
    public static double milesToMeters(double miles) {
        Quantity<Length> distanceInMiles = Quantities.getQuantity(miles, MILE);
        Quantity<Length> distanceInMeters = distanceInMiles.to(Units.METRE);

        return distanceInMeters.getValue().doubleValue();
    }

    /**
     * Converts a length measurement from Meters to Miles.
     *
     * @param meters The distance value in SI Meters
     * @return The mathematically converted distance in US Customary Miles
     */
    public static double metersToMiles(double meters) {
        Quantity<Length> distanceInMeters = Quantities.getQuantity(meters, Units.METRE);
        Quantity<Length> distanceInMiles = distanceInMeters.to(MILE);
        return distanceInMiles.getValue().doubleValue();
    }

    /**
     * 🌐 High-Precision Haversine Geodetic Calculator
     * Computes the offline great-circle distance between two GPS latitude/longitude
     * coordinate pairs over the earth's curved surface.
     *
     * @return A type-safe JSR 385 Length Quantity representing the physical distance
     */
    public static Quantity<Length> calculateHaversineDistance(
            double lat1, double lon1, double lat2, double lon2) {

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceInMeters = EARTH_RADIUS_METERS * c;

        return Quantities.getQuantity(distanceInMeters, Units.METRE);
    }
}