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
}