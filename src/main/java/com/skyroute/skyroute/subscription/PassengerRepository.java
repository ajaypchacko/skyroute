package com.skyroute.skyroute.subscription;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    // Spring builds: SELECT * FROM passengers WHERE public_id = ?
    Optional<Passenger> findByPublicId(UUID publicId);
}
