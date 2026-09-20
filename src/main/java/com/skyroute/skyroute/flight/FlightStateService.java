package com.skyroute.skyroute.flight;

import com.skyroute.skyroute.disruption.DisruptionEvent;
import com.skyroute.skyroute.disruption.DisruptionPolicy;
import com.skyroute.skyroute.disruption.DisruptionPublisher;
import com.skyroute.skyroute.ingest.FlightEvent;
import com.skyroute.skyroute.ingest.FlightEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The brain of Phase 3: applies one flight event to the flight in the database.
 */
@Service
public class FlightStateService {

    private static final Logger log = LoggerFactory.getLogger(FlightStateService.class);

    private final FlightRepository flightRepository;
    private final DisruptionPublisher disruptionPublisher;

    public FlightStateService(FlightRepository flightRepository, DisruptionPublisher disruptionPublisher) {
        this.flightRepository = flightRepository;
        this.disruptionPublisher = disruptionPublisher;
    }

    // @Transactional: everything in this method is ONE database transaction.
    // If anything throws (including a failed Kafka publish), the flight update is rolled back.
    @Transactional
    public void apply(FlightEvent event) {

        // 1. Find the flight. Unknown flight = a problem message (dead-letter topic in Phase 4).
        Flight flight = flightRepository.findByFlightNumber(event.flightNumber())
                .orElseThrow(() -> new IllegalArgumentException("Unknown flight: " + event.flightNumber()));

        // 2. Out-of-order guard: ignore anything not newer than what we already applied.
        if (flight.isStale(event.occurredAt())) {
            log.info("Ignoring stale event {} for {}: event time {} is not newer than {}",
                    event.eventId(), event.flightNumber(), event.occurredAt(), flight.getLastEventAt());
            return;
        }

        // 3. Apply the change. The status name matches the event type (DELAYED / CANCELLED / ON_TIME).
        int delay = event.type() == FlightEventType.DELAYED ? event.delayMinutes() : 0;
        flight.applyUpdate(event.type().name(), delay, event.occurredAt());
        flightRepository.save(flight);
        log.info("Updated {} to {} (delay {} min)", flight.getFlightNumber(), flight.getStatus(), delay);

        // 4. Threshold rule: only big changes go to the disruptions topic.
        if (DisruptionPolicy.isDisruption(event.type(), delay)) {
            disruptionPublisher.publish(new DisruptionEvent(
                    event.eventId(),
                    flight.getFlightNumber(),
                    flight.getOrigin(),
                    flight.getDestination(),
                    flight.getScheduledDeparture(),
                    event.type(),
                    delay,
                    event.occurredAt()));
        } else {
            log.info("{} is below the disruption threshold, no notification needed", flight.getFlightNumber());
        }
    }
}