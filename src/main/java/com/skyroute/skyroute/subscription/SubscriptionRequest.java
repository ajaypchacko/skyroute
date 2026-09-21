package com.skyroute.skyroute.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The JSON a visitor sends to POST /api/subscriptions.
 * The error messages are short because the field name is added in front of them.
 */
public record SubscriptionRequest(

        @NotBlank(message = "is required")
        @Size(max = 100, message = "must be 100 characters or fewer")
        String name,

        @NotBlank(message = "is required")
        @Pattern(regexp = "^[A-Z]{2}\\d{1,4}$", message = "must look like SR101")
        String flightNumber,

        // Australian mobile: +614 followed by 8 digits, e.g. +61412345678
        @NotBlank(message = "is required")
        @Pattern(regexp = "^\\+614\\d{8}$", message = "must look like +614XXXXXXXX")
        String mobileNumber) {
}
