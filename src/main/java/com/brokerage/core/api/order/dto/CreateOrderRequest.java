package com.brokerage.core.api.order.dto;

import com.brokerage.core.base.enumaration.OrderSide;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID customerId,
        @NotBlank String assetName,
        @NotNull OrderSide orderSide,
        @NotNull @DecimalMin("0.0001") BigDecimal size,
        @NotNull @DecimalMin("0.01") BigDecimal price
) {}

