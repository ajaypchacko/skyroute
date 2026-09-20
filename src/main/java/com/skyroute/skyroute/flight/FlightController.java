package com.skyroute.skyroute.flight;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Handles web requests that start with /api/flights.
 */
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightRepository flightRepository;

    // Constructor injection: Spring sees this parameter and passes in the real
    // repository, so we never write "new FlightRepository()" ourselves.
    public FlightController(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    // Runs when someone sends GET /api/flights
    @GetMapping
    public List<FlightResponse> getFlights() {
        return flightRepository.findAll(Sort.by("scheduledDeparture"))
                .stream()
                .map(FlightResponse::from)
                .toList();
    }
}