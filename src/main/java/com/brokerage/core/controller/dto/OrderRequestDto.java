package com.brokerage.core.controller.dto;

import com.brokerage.core.enumaration.OrderSide;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderRequestDto(
        @NotNull UUID customerId,
        @NotBlank String assetName,
        @NotNull OrderSide side,
        @DecimalMin("0.0001") BigDecimal size,
        @DecimalMin("0.01") BigDecimal price
) {}

