package com.skyroute.skyroute.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Admin-only actions and lookups. Every path under /api/admin/ needs the X-API-Key
 * header (checked by AdminApiKeyFilter before a request ever gets here).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DemoResetService demoResetService;
    private final AdminQueryService adminQueryService;

    public AdminController(DemoResetService demoResetService, AdminQueryService adminQueryService) {
        this.demoResetService = demoResetService;
        this.adminQueryService = adminQueryService;
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {
        demoResetService.reset();
        return Map.of("status", "reset");
    }

    // Every registered passenger and the flight they are booked on.
    @GetMapping("/passengers")
    public List<AdminPassengerView> passengers() {
        return adminQueryService.listPassengers();
    }

    // Who was notified for one flight, newest first. Lets the operations desk show
    // the result of a Send delay / Cancel / Mark on time action.
    @GetMapping("/flights/{flightNumber}/notifications")
    public List<AdminNotificationView> notificationsForFlight(@PathVariable String flightNumber) {
        return adminQueryService.notificationsForFlight(flightNumber);
    }
}