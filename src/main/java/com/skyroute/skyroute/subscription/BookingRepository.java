package com.skyroute.skyroute.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Spring builds: SELECT * FROM bookings WHERE flight_id = ?
    // We will use this in Phase 4 to find everyone booked on a disrupted flight.
    List<Booking> findByFlightId(Long flightId);
}
