package com.skyroute.skyroute.notification;

import com.skyroute.skyroute.config.KafkaTopics;
import com.skyroute.skyroute.disruption.DisruptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Listens to the "disruptions" topic. This is a different consumer group from the
 * flight-state consumer, so each group gets its own copy of the messages it cares about.
 */
@Component
public class DisruptionEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DisruptionEventConsumer.class);

    private final NotificationService notificationService;

    public DisruptionEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // The global setting says "messages are FlightEvent". This topic carries DisruptionEvent,
    // so this one listener overrides the target class for its own consumer.
    @KafkaListener(
            topics = KafkaTopics.DISRUPTIONS,
            groupId = "notifications",
            properties = "spring.json.value.default.type=com.skyroute.skyroute.disruption.DisruptionEvent")
    public void onMessage(DisruptionEvent event) {
        log.info("Received disruption {} for {}", event.type(), event.flightNumber());
        notificationService.notifyPassengers(event);
    }
}