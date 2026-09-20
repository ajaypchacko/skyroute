package com.skyroute.skyroute.config;

/**
 * Topic names in one place, so we never mistype a string in two files.
 */
public final class KafkaTopics {

    public static final String FLIGHT_EVENTS = "flight-events";
    public static final String DISRUPTIONS = "disruptions";

    private KafkaTopics() {
        // utility class: nobody should create an instance
    }
}