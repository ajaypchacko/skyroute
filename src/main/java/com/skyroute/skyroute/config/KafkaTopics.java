package com.skyroute.skyroute.config;

/**
 * Topic names in one place, so we never mistype a string in two files.
 */
public final class KafkaTopics {

    public static final String FLIGHT_EVENTS = "flight-events";
    public static final String DISRUPTIONS = "disruptions";

    // Dead-letter topics: where messages go when they cannot be processed.
    // The ".DLT" suffix is the Spring Kafka convention (original topic name + ".DLT").
    public static final String FLIGHT_EVENTS_DLT = "flight-events.DLT";
    public static final String DISRUPTIONS_DLT = "disruptions.DLT";

    private KafkaTopics() {
        // utility class: nobody should create an instance
    }
}