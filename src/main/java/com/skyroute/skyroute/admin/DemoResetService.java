package com.skyroute.skyroute.admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puts the demo back to its starting state so it can be shown again.
 * It only touches the database. It does not clear Kafka topics.
 */
@Service
public class DemoResetService {

    private static final Logger log = LoggerFactory.getLogger(DemoResetService.class);

    // The seed data (migration V2) created passengers with ids 1 to 100.
    // Anyone with a higher id signed up through the website, so reset removes them.
    private static final int SEED_PASSENGER_COUNT = 100;

    @PersistenceContext
    private EntityManager entityManager;

    // All four steps run in ONE transaction: the reset happens completely, or not at all.
    @Transactional
    public void reset() {

        entityManager.createNativeQuery("DELETE FROM notifications").executeUpdate();

        entityManager.createNativeQuery("DELETE FROM bookings WHERE passenger_id > :seedMax")
                .setParameter("seedMax", SEED_PASSENGER_COUNT)
                .executeUpdate();

        entityManager.createNativeQuery("DELETE FROM passengers WHERE id > :seedMax")
                .setParameter("seedMax", SEED_PASSENGER_COUNT)
                .executeUpdate();

        // Every flight back to ON_TIME, with departures again 2 to 21 hours from now.
        // SR101 -> 2 hours, SR102 -> 3 hours ... SR120 -> 21 hours (same pattern as the seed data).
        entityManager.createNativeQuery("""
                UPDATE flights
                SET status = 'ON_TIME',
                    delay_minutes = 0,
                    last_event_at = NULL,
                    scheduled_departure = date_trunc('hour', now())
                        + (CAST(substring(flight_number from 3) AS integer) - 99) * interval '1 hour'
                """).executeUpdate();

        log.info("Demo data reset");
    }
}
