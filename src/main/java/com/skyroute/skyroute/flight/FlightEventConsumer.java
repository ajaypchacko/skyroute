package com.skyroute.skyroute.flight;

import com.skyroute.skyroute.config.KafkaTopics;
import com.skyroute.skyroute.ingest.FlightEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to the "flight-events" topic. Spring calls onMessage() once for every
 * message. The real work is in FlightStateService, which is a separate bean so that
 * its @Transactional annotation takes effect.
 */
@Component
public class FlightEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FlightEventConsumer.class);

    private final FlightStateService flightStateService;

    public FlightEventConsumer(FlightStateService flightStateService) {
        this.flightStateService = flightStateService;
    }

    @KafkaListener(topics = KafkaTopics.FLIGHT_EVENTS, groupId = "flight-state")
    public void onMessage(FlightEvent event) {
        log.info("Received {} for {} (event {})", event.type(), event.flightNumber(), event.eventId());
        flightStateService.apply(event);
    }
}