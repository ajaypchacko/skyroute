package com.skyroute.skyroute.admin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin-only actions. Every path under /api/admin/ needs the X-API-Key header
 * (checked by AdminApiKeyFilter before a request ever gets here).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DemoResetService demoResetService;

    public AdminController(DemoResetService demoResetService) {
        this.demoResetService = demoResetService;
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {
        demoResetService.reset();
        return Map.of("status", "reset");
    }
}
