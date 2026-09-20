package com.skyroute.skyroute.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Pretends to send an SMS by writing it to the log. No real message is ever sent.
 */
@Component
public class MockNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(MockNotificationSender.class);

    @Override
    public void send(String mobileNumber, String message) {
        log.info("[MOCK SMS] to {}: {}", mobileNumber, message);
    }
}