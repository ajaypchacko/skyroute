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

    // Time of the newest event applied to this flight. Can be null (no event yet).
    @Column(name = "last_event_at")
    private Instant lastEventAt;

    // JPA needs an empty constructor to create objects itself. "protected" stops
    // the rest of our code from using it by accident.
    protected Flight() {
    }

    /**
     * Out-of-order guard: an event is "stale" if it is not NEWER than the last
     * event we already applied. This also makes a repeated (redelivered) event harmless.
     */
    public boolean isStale(Instant eventTime) {
        return lastEventAt != null && !eventTime.isAfter(lastEventAt);
    }

    /**
     * Applies a new state and remembers the time of the event that caused it.
     */
    public void applyUpdate(String newStatus, int newDelayMinutes, Instant eventTime) {
        this.status = newStatus;
        this.delayMinutes = newDelayMinutes;
        this.lastEventAt = eventTime;
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