package com.brokerage.core.controller.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Request body for listing orders in a specific date range for a customer.
 */
public record ListOrdersRequest(

        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotNull(message = "start date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime start,

        @NotNull(message = "end date is required")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime end
) {}

