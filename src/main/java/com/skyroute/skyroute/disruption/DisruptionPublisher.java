package com.skyroute.skyroute.disruption;

import com.skyroute.skyroute.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Puts disruption events into the "disruptions" topic.
 */
@Service
public class DisruptionPublisher {

    private static final Logger log = LoggerFactory.getLogger(DisruptionPublisher.class);

    private final KafkaTemplate<String, DisruptionEvent> kafkaTemplate;

    public DisruptionPublisher(KafkaTemplate<String, DisruptionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(DisruptionEvent event) {
        try {
            // Same key idea as before: one flight = one partition = ordered.
            // We wait for Kafka to confirm. If it fails we throw, which rolls back the
            // database change and makes Kafka redeliver the original event to retry.
            kafkaTemplate.send(KafkaTopics.DISRUPTIONS, event.flightNumber(), event)
                    .get(5, TimeUnit.SECONDS);
            log.info("Published disruption {} for {}", event.type(), event.flightNumber());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing disruption", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new IllegalStateException("Could not publish disruption for " + event.flightNumber(), e);
        }
    }
}