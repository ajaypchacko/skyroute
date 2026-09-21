package com.skyroute.skyroute.admin;

import java.time.Instant;

/**
 * One row of the admin "who was notified" list, for a single flight.
 */
public record AdminNotificationView(String name, String mobileNumber, String message, Instant createdAt) {
}
