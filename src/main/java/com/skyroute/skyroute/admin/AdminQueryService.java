package com.skyroute.skyroute.admin;

import com.skyroute.skyroute.flight.Flight;
import com.skyroute.skyroute.flight.FlightRepository;
import com.skyroute.skyroute.notification.Notification;
import com.skyroute.skyroute.notification.NotificationRepository;
import com.skyroute.skyroute.subscription.Booking;
import com.skyroute.skyroute.subscription.BookingRepository;
import com.skyroute.skyroute.subscription.Passenger;
import com.skyroute.skyroute.subscription.PassengerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-only lookups for the operations side of the website.
 * This is a small demo dataset, so both methods load everything into memory and
 * join it in Java rather than writing extra SQL joins.
 */
@Service
public class AdminQueryService {

    private static final int MAX_PASSENGERS_SHOWN = 200;

    private final FlightRepository flightRepository;
    private final PassengerRepository passengerRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;

    public AdminQueryService(FlightRepository flightRepository,
                             PassengerRepository passengerRepository,
                             BookingRepository bookingRepository,
                             NotificationRepository notificationRepository) {
        this.flightRepository = flightRepository;
        this.passengerRepository = passengerRepository;
        this.bookingRepository = bookingRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Every booking, newest first, with the passenger's and flight's details attached.
     */
    public List<AdminPassengerView> listPassengers() {
        Map<Long, String> flightNumberById = flightRepository.findAll().stream()
                .collect(Collectors.toMap(Flight::getId, Flight::getFlightNumber));

        Map<Long, Passenger> passengerById = passengerRepository.findAll().stream()
                .collect(Collectors.toMap(Passenger::getId, Function.identity()));

        return bookingRepository.findAll().stream()
                .sorted(Comparator.comparing(Booking::getId).reversed())   // newest booking first
                .limit(MAX_PASSENGERS_SHOWN)
                .map(booking -> {
                    Passenger passenger = passengerById.get(booking.getPassengerId());
                    String flightNumber = flightNumberById.get(booking.getFlightId());
                    return new AdminPassengerView(passenger.getName(), passenger.getMobileNumber(), flightNumber);
                })
                .toList();
    }

    /**
     * Every notification sent for one flight, newest first.
     * Used by the operations desk to show who received a given update.
     */
    public List<AdminNotificationView> notificationsForFlight(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown flight: " + flightNumber));

        List<Notification> notifications = notificationRepository.findByFlightIdOrderByCreatedAtDesc(flight.getId());

        Map<Long, Passenger> passengerById = passengerRepository
                .findAllById(notifications.stream().map(Notification::getPassengerId).toList())
                .stream()
                .collect(Collectors.toMap(Passenger::getId, Function.identity()));

        return notifications.stream()
                .map(n -> {
                    Passenger passenger = passengerById.get(n.getPassengerId());
                    return new AdminNotificationView(passenger.getName(), passenger.getMobileNumber(), n.getMessage(), n.getCreatedAt());
                })
                .toList();
    }
}
