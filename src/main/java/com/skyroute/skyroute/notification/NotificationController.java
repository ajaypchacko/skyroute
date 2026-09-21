package com.skyroute.skyroute.notification;

import com.skyroute.skyroute.subscription.Passenger;
import com.skyroute.skyroute.subscription.PassengerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * "My notifications": a passenger reads only their own messages, using their random public id.
 * There is deliberately no endpoint that lists all passengers or all notifications.
 */
@RestController
@RequestMapping("/api/passengers")
public class NotificationController {

    private final PassengerRepository passengerRepository;
    private final NotificationRepository notificationRepository;

    public NotificationController(PassengerRepository passengerRepository,
                                  NotificationRepository notificationRepository) {
        this.passengerRepository = passengerRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/{passengerId}/notifications")
    public List<NotificationResponse> getNotifications(@PathVariable UUID passengerId) {

        Passenger passenger = passengerRepository.findByPublicId(passengerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Passenger not found"));

        return notificationRepository.findByPassengerIdOrderByCreatedAtDesc(passenger.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }
}
