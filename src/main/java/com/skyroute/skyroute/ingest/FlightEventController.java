package com.skyroute.skyroute.ingest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/**
 * Receives flight events, checks them, and hands them to Kafka.
 * It does NOT update the flight itself. A separate consumer does that (Phase 3).
 *
 * NOTE: not protected yet. In Phase 5 we add the admin API key.
 */
@RestController
@RequestMapping("/api/flight-events")
public class FlightEventController {

    private final FlightEventProducer producer;

    public FlightEventController(FlightEventProducer producer) {
        this.producer = producer;
    }

    // 202 Accepted = "we received it and queued it", not "it is finished".
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public FlightEvent receive(@Valid @RequestBody FlightEventRequest request) {

        int delay = request.delayMinutes() == null ? 0 : request.delayMinutes();

        if (request.type() == FlightEventType.DELAYED && delay <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A DELAYED event needs delayMinutes greater than 0");
        }

        Instant occurredAt = request.occurredAt() != null ? request.occurredAt() : Instant.now();

        FlightEvent event = new FlightEvent(
                UUID.randomUUID(),
                request.flightNumber(),
                request.type(),
                delay,
                occurredAt);

        producer.publish(event);
        return event;   // echo back what we queued, including the generated eventId
    }
}
