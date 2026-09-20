package com.skyroute.skyroute.ingest;

import com.skyroute.skyroute.config.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Puts flight events into Kafka.
 */
@Service
public class FlightEventProducer {

    private static final Logger log = LoggerFactory.getLogger(FlightEventProducer.class);

    private final KafkaTemplate<String, FlightEvent> kafkaTemplate;

    public FlightEventProducer(KafkaTemplate<String, FlightEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(FlightEvent event) {
        try {
            // The message KEY is the flight number. Kafka sends every message with the
            // same key to the same partition, and keeps the order inside a partition.
            // So all events for one flight are read in the order they were sent.
            //
            // send() is asynchronous. We wait up to 5 seconds for Kafka to confirm it
            // stored the message, so the caller never gets a "success" for a lost event.
            var result = kafkaTemplate
                    .send(KafkaTopics.FLIGHT_EVENTS, event.flightNumber(), event)
                    .get(5, TimeUnit.SECONDS);

            log.info("Published {} for {} to partition {}, offset {}",
                    event.type(), event.flightNumber(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Interrupted while publishing event");
        } catch (ExecutionException | TimeoutException e) {
            log.error("Could not publish event {}", event.eventId(), e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not publish event to Kafka");
        }
    }
}
