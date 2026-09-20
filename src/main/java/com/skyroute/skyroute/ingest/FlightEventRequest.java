package com.skyroute.skyroute.ingest;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;

/**
 * The JSON that a caller (the admin page, curl, a simulator) sends to POST /api/flight-events.
 * The annotations are the validation rules. If a rule fails, Spring answers 400 Bad Request
 * and our code never runs.
 */
public record FlightEventRequest(

        @NotBlank(message = "flightNumber is required")
        @Pattern(regexp = "^[A-Z]{2}\\d{1,4}$", message = "flightNumber must look like SR101")
        String flightNumber,

        @NotNull(message = "type is required (DELAYED, CANCELLED or ON_TIME)")
        FlightEventType type,

        @Min(value = 0, message = "delayMinutes cannot be negative")
        @Max(value = 1440, message = "delayMinutes cannot be more than 1440 (24 hours)")
        Integer delayMinutes,          // Integer (not int) so it can be left out

        Instant occurredAt) {          // optional: lets us test old, out-of-order events later
}
