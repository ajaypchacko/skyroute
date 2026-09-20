package com.skyroute.skyroute.disruption;

import com.skyroute.skyroute.ingest.FlightEventType;

/**
 * The business rule: "is this change big enough to notify passengers?"
 * Kept in its own small class so it is easy to unit test (Phase 7).
 */
public final class DisruptionPolicy {

    public static final int DELAY_THRESHOLD_MINUTES = 30;

    private DisruptionPolicy() {
    }

    public static boolean isDisruption(FlightEventType type, int delayMinutes) {
        return type == FlightEventType.CANCELLED
                || (type == FlightEventType.DELAYED && delayMinutes >= DELAY_THRESHOLD_MINUTES);
    }
}