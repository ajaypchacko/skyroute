package com.skyroute.skyroute.notification;

import java.time.Instant;

/**
 * What a passenger sees for each notification: the text and when it was created.
 */
public record NotificationResponse(String message, Instant createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getMessage(), notification.getCreatedAt());
    }
}
