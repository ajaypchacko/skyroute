package com.skyroute.skyroute.disruption;

import com.skyroute.skyroute.ingest.FlightEventType;

import java.time.Instant;
import java.util.UUID;

/**
 * The message published to the "disruptions" topic when a flight change is big
 * enough to tell passengers about. It carries everything the notification step
 * needs, so that step doesn't have to ask the flight service for more details.
 */
public record DisruptionEvent(
        UUID sourceEventId,          // the flight event that caused this disruption
        String flightNumber,
        String origin,
        String destination,
        Instant scheduledDeparture,
        FlightEventType type,        // DELAYED or CANCELLED
        int delayMinutes,
        Instant occurredAt) {
}