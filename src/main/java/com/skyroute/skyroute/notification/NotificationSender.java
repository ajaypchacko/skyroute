package com.skyroute.skyroute.notification;

/**
 * How a message reaches a passenger. An interface, so the delivery channel is swappable:
 * today a mock that only logs, later (optionally) a real SMS service such as AWS SNS.
 */
public interface NotificationSender {

    void send(String mobileNumber, String message);
}