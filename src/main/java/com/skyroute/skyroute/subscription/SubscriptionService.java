package com.skyroute.skyroute.subscription;

import com.skyroute.skyroute.flight.Flight;
import com.skyroute.skyroute.flight.FlightRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Registers a passenger for alerts on one flight.
 */
@Service
public class SubscriptionService {

    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;

    public SubscriptionService(FlightRepository flightRepository,
                               PassengerRepository passengerRepository,
                               BookingRepository bookingRepository) {
        this.flightRepository = flightRepository;
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
    }

    // One transaction: the passenger and the booking are saved together, or not at all.
    @Transactional
    public SubscriptionResponse subscribe(SubscriptionRequest request) {

        Flight flight = flightRepository.findByFlightNumber(request.flightNumber())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Unknown flight: " + request.flightNumber()));

        // Every sign-up creates a NEW passenger. We never look someone up by phone number,
        // because that would let a stranger who knows a number see that person's notifications.
        Passenger passenger = passengerRepository.save(
                new Passenger(request.name().trim(), request.mobileNumber()));

        bookingRepository.save(new Booking(passenger.getId(), flight.getId()));

        return new SubscriptionResponse(passenger.getPublicId(), passenger.getName(), flight.getFlightNumber());
    }
}
