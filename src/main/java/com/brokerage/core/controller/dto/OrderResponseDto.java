package com.brokerage.core.controller.dto;

import com.brokerage.core.enumaration.OrderSide;
import com.brokerage.core.enumaration.OrderStatus;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponseDto(
        UUID id,
        UUID customerId,
        String assetName,
        OrderSide side,
        BigDecimal size,
        BigDecimal price,
        OrderStatus status,
        LocalDateTime createDate
) {}

