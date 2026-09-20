package com.skyroute.skyroute.flight;

import java.time.Instant;

/**
 * The JSON shape we send to API callers.
 * A record is a short way to write a plain data class: Java creates the
 * constructor and accessors for us.
 */
public record FlightResponse(
        String flightNumber,
        String origin,
        String destination,
        Instant scheduledDeparture,
        String status,
        int delayMinutes) {

    // Converts a database Flight into the response shape.
    public static FlightResponse from(Flight flight) {
        return new FlightResponse(
                flight.getFlightNumber(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getScheduledDeparture(),
                flight.getStatus(),
                flight.getDelayMinutes());
    }
}
