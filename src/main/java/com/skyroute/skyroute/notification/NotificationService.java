package com.skyroute.skyroute.notification;

import com.skyroute.skyroute.disruption.DisruptionEvent;
import com.skyroute.skyroute.flight.Flight;
import com.skyroute.skyroute.flight.FlightRepository;
import com.skyroute.skyroute.subscription.Booking;
import com.skyroute.skyroute.subscription.BookingRepository;
import com.skyroute.skyroute.subscription.Passenger;
import com.skyroute.skyroute.subscription.PassengerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Turns one disruption into one notification per booked passenger, at most once each.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final PassengerRepository passengerRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationSender sender;

    public NotificationService(FlightRepository flightRepository,
                               BookingRepository bookingRepository,
                               PassengerRepository passengerRepository,
                               NotificationRepository notificationRepository,
                               NotificationSender sender) {
        this.flightRepository = flightRepository;
        this.bookingRepository = bookingRepository;
        this.passengerRepository = passengerRepository;
        this.notificationRepository = notificationRepository;
        this.sender = sender;
    }

    // One transaction: if anything fails part-way, ALL the inserts are undone and the
    // message is retried, so we never end up with half the passengers notified in the database.
    @Transactional
    public void notifyPassengers(DisruptionEvent event) {

        Flight flight = flightRepository.findByFlightNumber(event.flightNumber())
                .orElseThrow(() -> new IllegalArgumentException("Unknown flight: " + event.flightNumber()));

        List<Booking> bookings = bookingRepository.findByFlightId(flight.getId());

        // Load all the passengers in one query instead of one query per booking.
        Map<Long, Passenger> passengersById = passengerRepository
                .findAllById(bookings.stream().map(Booking::getPassengerId).toList())
                .stream()
                .collect(Collectors.toMap(Passenger::getId, Function.identity()));

        String message = NotificationMessages.build(event);
        int notified = 0;
        int duplicates = 0;

        for (Booking booking : bookings) {
            Passenger passenger = passengersById.get(booking.getPassengerId());
            String dedupeKey = NotificationMessages.dedupeKey(passenger.getId(), event);

            // 1 = new notification saved, 0 = this passenger already has it
            int inserted = notificationRepository.insertIfAbsent(
                    passenger.getId(), flight.getId(), message, dedupeKey);

            if (inserted == 1) {
                sender.send(passenger.getMobileNumber(), message);
                notified++;
            } else {
                duplicates++;
            }
        }

        log.info("Disruption {} for {}: {} passengers notified, {} duplicates skipped",
                event.type(), event.flightNumber(), notified, duplicates);
    }
}
