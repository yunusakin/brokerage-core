package com.brokerage.core.api.customer.dto;

import com.brokerage.core.base.enumaration.Role;
import java.util.UUID;

public record CustomerDto(
        UUID id,
        String username,
        String password,
        Role role
) {}

