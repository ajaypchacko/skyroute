package com.skyroute.skyroute.flight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * No code needed inside: Spring generates the implementation at startup.
 * JpaRepository<Flight, Long> means "manage Flight objects whose id is a Long".
 * It gives us findAll(), findById(), save(), delete() and more for free.
 */
public interface FlightRepository extends JpaRepository<Flight, Long> {

    // Spring reads the method NAME and builds the SQL:
    // SELECT * FROM flights WHERE flight_number = ?
    Optional<Flight> findByFlightNumber(String flightNumber);
}
