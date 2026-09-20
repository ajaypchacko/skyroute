package com.skyroute.skyroute.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One row of the "bookings" table: "this passenger is on this flight".
 * We store the two ids as plain numbers instead of linking to Passenger and
 * Flight objects. It is simpler, and all we need is "who is on flight X?".
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    protected Booking() {
    }

    public Booking(Long passengerId, Long flightId) {
        this.passengerId = passengerId;
        this.flightId = flightId;
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
}
