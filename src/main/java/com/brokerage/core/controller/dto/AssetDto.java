package com.brokerage.core.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AssetDto(
        UUID id,
        UUID customerId,
        String assetName,
        BigDecimal size,
        BigDecimal usableSize
) {}

