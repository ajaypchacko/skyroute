package com.skyroute.skyroute.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One row of the "notifications" table: "this passenger was told this message".
 * Rows are created by NotificationRepository.insertIfAbsent (a single SQL statement),
 * so this class is only used for reading them back.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "dedupe_key", nullable = false, unique = true)
    private String dedupeKey;

    protected Notification() {
    }

    public Long getId() {
        return id;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public String getMessage() {
        return message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getDedupeKey() {
        return dedupeKey;
    }
}