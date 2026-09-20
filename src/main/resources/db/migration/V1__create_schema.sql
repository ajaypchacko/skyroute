-- V1: core schema for SkyRoute

CREATE TABLE flights (
    id                  BIGSERIAL PRIMARY KEY,
    flight_number       VARCHAR(10)  NOT NULL UNIQUE,
    origin              VARCHAR(3)   NOT NULL,
    destination         VARCHAR(3)   NOT NULL,
    scheduled_departure TIMESTAMPTZ  NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'ON_TIME',
    delay_minutes       INT          NOT NULL DEFAULT 0,
    last_event_at       TIMESTAMPTZ            -- time of the newest event applied (out-of-order guard, Phase 3)
);

CREATE TABLE passengers (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    mobile_number VARCHAR(20)  NOT NULL
);

CREATE TABLE bookings (
    id           BIGSERIAL PRIMARY KEY,
    passenger_id BIGINT NOT NULL REFERENCES passengers (id),
    flight_id    BIGINT NOT NULL REFERENCES flights (id),
    CONSTRAINT uq_booking UNIQUE (passenger_id, flight_id)   -- a passenger can't be booked twice on one flight
);

CREATE INDEX idx_bookings_flight_id ON bookings (flight_id);  -- "who is on flight X?" must be fast

CREATE TABLE notifications (
    id           BIGSERIAL PRIMARY KEY,
    passenger_id BIGINT       NOT NULL REFERENCES passengers (id),
    flight_id    BIGINT       NOT NULL REFERENCES flights (id),
    message      VARCHAR(500) NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    dedupe_key   VARCHAR(200) NOT NULL,
    CONSTRAINT uq_notification_dedupe UNIQUE (dedupe_key)    -- the database itself refuses duplicates (Phase 4)
);

CREATE INDEX idx_notifications_passenger_id ON notifications (passenger_id);
