package com.skyroute.skyroute.flight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One row of the "flights" table.
 * Each field below matches one column. JPA/Hibernate uses this class to
 * load rows from the database and turn them into Java objects.
 */
@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // the database generates the id (BIGSERIAL)
    private Long id;

    @Column(name = "flight_number", nullable = false, unique = true)
    private String flightNumber;

    @Column(nullable = false)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @Column(name = "scheduled_departure", nullable = false)
    private Instant scheduledDeparture;

    @Column(nullable = false)
    private String status;

    @Column(name = "delay_minutes", nullable = false)
    private int delayMinutes;

    // Time of the newest event applied to this flight (used in Phase 3). Can be null.
    @Column(name = "last_event_at")
    private Instant lastEventAt;

    // JPA needs an empty constructor to create objects itself. "protected" stops
    // the rest of our code from using it by accident.
    protected Flight() {
    }

    public Long getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public Instant getScheduledDeparture() {
        return scheduledDeparture;
    }

    public String getStatus() {
        return status;
    }

    public int getDelayMinutes() {
        return delayMinutes;
    }

    public Instant getLastEventAt() {
        return lastEventAt;
    }
}
