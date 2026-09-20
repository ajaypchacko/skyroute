package com.skyroute.skyroute.notification;

import com.skyroute.skyroute.disruption.DisruptionEvent;
import com.skyroute.skyroute.ingest.FlightEventType;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builds the text of a notification and its dedupe key.
 * Plain static methods with no database or Kafka, so they are easy to unit test (Phase 7).
 */
public final class NotificationMessages {

    // Demo simplification: show all times in Melbourne time.
    // A real system would use the departure airport's own time zone.
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Australia/Melbourne");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("d MMM HH:mm", Locale.ENGLISH);

    private NotificationMessages() {
    }

    public static String build(DisruptionEvent event) {
        String route = event.origin() + " to " + event.destination();

        if (event.type() == FlightEventType.CANCELLED) {
            return "SkyRoute: Your flight %s (%s) has been cancelled. Please check your booking for options."
                    .formatted(event.flightNumber(), route);
        }

        Instant newDeparture = event.scheduledDeparture().plus(Duration.ofMinutes(event.delayMinutes()));
        return "SkyRoute: Your flight %s (%s) is delayed by %d minutes. New departure: %s."
                .formatted(event.flightNumber(), route, event.delayMinutes(),
                        TIME_FORMAT.format(newDeparture.atZone(DISPLAY_ZONE)));
    }

    /**
     * Same passenger + same flight + same kind of disruption + same delay = same key.
     *
     * This catches BOTH kinds of duplicate:
     *  1. Kafka delivering the same message twice (identical event).
     *  2. An admin sending the same update twice (a NEW event, but the same effect).
     *
     * A different delay (45 then 60) gives a different key, so passengers ARE told about
     * a genuine change. Trade-off: if a flight recovers and then gets the exact same delay
     * again, that repeat will not notify. That is a deliberate simplification.
     */
    public static String dedupeKey(Long passengerId, DisruptionEvent event) {
        return passengerId + ":" + event.flightNumber() + ":" + event.type() + ":" + event.delayMinutes();
    }
}