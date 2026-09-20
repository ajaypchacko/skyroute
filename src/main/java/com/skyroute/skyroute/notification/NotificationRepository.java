package com.skyroute.skyroute.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Used by the "my notifications" endpoint in Phase 5.
    List<Notification> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);

    /**
     * The heart of "never notify twice".
     * ONE atomic SQL statement: insert the row, unless a row with the same dedupe_key exists.
     * Returns 1 if it inserted, 0 if it was a duplicate.
     *
     * Why not "check if it exists, then insert"? Two consumers could both check at the same
     * moment, both see "not there", and both insert. The unique constraint in the database
     * plus ON CONFLICT makes the database itself decide, so that race cannot happen.
     */
    @Modifying
    @Query(value = """
            INSERT INTO notifications (passenger_id, flight_id, message, dedupe_key)
            VALUES (:passengerId, :flightId, :message, :dedupeKey)
            ON CONFLICT (dedupe_key) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("passengerId") Long passengerId,
                       @Param("flightId") Long flightId,
                       @Param("message") String message,
                       @Param("dedupeKey") String dedupeKey);
}