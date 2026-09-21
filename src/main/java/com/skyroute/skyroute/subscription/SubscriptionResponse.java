package com.skyroute.skyroute.subscription;

import java.util.UUID;

/**
 * What we send back after a sign-up. The phone number is NOT echoed back.
 * passengerId is the random public id, used to look up this passenger's notifications.
 */
public record SubscriptionResponse(UUID passengerId, String name, String flightNumber) {
}
