package com.skyroute.skyroute.admin;

/**
 * One row of the admin "registered passengers" list: a passenger and one flight
 * they are booked on. A passenger with two bookings appears as two rows.
 */
public record AdminPassengerView(String name, String mobileNumber, String flightNumber) {
}
