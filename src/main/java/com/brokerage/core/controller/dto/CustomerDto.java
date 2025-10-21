package com.brokerage.core.controller.dto;

import com.brokerage.core.enumaration.Role;
import java.util.UUID;

public record CustomerDto(
        UUID id,
        String username,
        String password,
        Role role
) {}

