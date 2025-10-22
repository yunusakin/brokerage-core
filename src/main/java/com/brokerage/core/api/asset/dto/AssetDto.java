package com.brokerage.core.api.asset.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AssetDto(
        UUID id,
        UUID customerId,
        String assetName,
        BigDecimal size,
        BigDecimal usableSize
) {}

