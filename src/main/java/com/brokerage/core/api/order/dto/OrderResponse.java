package com.brokerage.core.api.order.dto;

import com.brokerage.core.base.enumaration.OrderSide;
import com.brokerage.core.base.enumaration.OrderStatus;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String assetName,
        OrderSide orderSide,
        BigDecimal size,
        BigDecimal price,
        OrderStatus status,
        LocalDateTime createDate
) {}

