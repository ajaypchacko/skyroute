package com.skyroute.skyroute.ingest;

import java.time.Instant;
import java.util.UUID;

/**
 * The message that travels through Kafka. This is the "contract" between the
 * part that sends events and the part that reads them.
 */
public record FlightEvent(
        UUID eventId,             // unique id for this one message
        String flightNumber,
        FlightEventType type,
        int delayMinutes,
        Instant occurredAt) {     // when it happened (used to ignore old events in Phase 3)
}
