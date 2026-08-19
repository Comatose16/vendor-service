-- 1. Ensure the column type is geometry(Point, 4326) to satisfy Hibernate validate
ALTER TABLE venues
ALTER COLUMN location TYPE GEOMETRY(Point, 4326) USING location::geometry;

-- 2. Drop the redundant or standard geometry index
DROP INDEX IF EXISTS idx_venues_location;
DROP INDEX IF EXISTS idx_venues_location_geography;

-- 3. Create the functional GiST index on the casted geography expression
CREATE INDEX idx_venues_location_geography
ON venues USING GIST (((location)::geography));