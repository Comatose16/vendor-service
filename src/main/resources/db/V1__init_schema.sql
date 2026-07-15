CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    address VARCHAR(255),
    location geometry(Point, 4326) NOT NULL
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(31) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    venue_id BIGINT NOT NULL,
    drink_specials_detail TEXT,
    food_specials_detail TEXT,
    condition_details TEXT,
    is_active BOOLEAN,
    CONSTRAINT fk_events_venue FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

CREATE INDEX idx_venues_location ON venues USING GIST (location);