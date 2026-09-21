package com.skyroute.skyroute.simulator;

import com.skyroute.skyroute.flight.Flight;
import com.skyroute.skyroute.flight.FlightRepository;
import com.skyroute.skyroute.ingest.FlightEvent;
import com.skyroute.skyroute.ingest.FlightEventProducer;
import com.skyroute.skyroute.ingest.FlightEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sends a random flight event every few seconds, so the demo looks alive without anyone clicking.
 * OFF by default. Switch it on with:  skyroute.simulator.enabled=true
 *
 * It publishes straight to Kafka through the same producer the API uses, so the events
 * travel the whole pipeline: consumer, disruption rule, notifications.
 */
@Component
@EnableScheduling
@ConditionalOnProperty(name = "skyroute.simulator.enabled", havingValue = "true")
public class FlightSimulator {

    private static final Logger log = LoggerFactory.getLogger(FlightSimulator.class);

    private static final int[] SMALL_DELAYS = {5, 10, 15, 20, 25};          // below the 30 minute rule
    private static final int[] BIG_DELAYS = {30, 45, 60, 90, 120};          // will notify passengers

    private final FlightRepository flightRepository;
    private final FlightEventProducer producer;

    public FlightSimulator(FlightRepository flightRepository, FlightEventProducer producer) {
        this.flightRepository = flightRepository;
        this.producer = producer;
    }

    @Scheduled(
            initialDelayString = "${skyroute.simulator.interval-ms:8000}",
            fixedDelayString = "${skyroute.simulator.interval-ms:8000}")
    public void sendRandomEvent() {
        List<Flight> flights = flightRepository.findAll();
        if (flights.isEmpty()) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        String flightNumber = flights.get(random.nextInt(flights.size())).getFlightNumber();

        // Mix of outcomes: mostly small delays, some big ones, a few cancellations and recoveries.
        int roll = random.nextInt(100);
        FlightEventType type;
        int delay = 0;
        if (roll < 50) {
            type = FlightEventType.DELAYED;
            delay = SMALL_DELAYS[random.nextInt(SMALL_DELAYS.length)];
        } else if (roll < 80) {
            type = FlightEventType.DELAYED;
            delay = BIG_DELAYS[random.nextInt(BIG_DELAYS.length)];
        } else if (roll < 90) {
            type = FlightEventType.CANCELLED;
        } else {
            type = FlightEventType.ON_TIME;
        }

        try {
            producer.publish(new FlightEvent(UUID.randomUUID(), flightNumber, type, delay, Instant.now()));
            log.info("[SIMULATOR] {} {} {}", flightNumber, type, delay);
        } catch (RuntimeException e) {
            log.warn("[SIMULATOR] could not publish an event: {}", e.getMessage());
        }
    }
}
