package com.brokerage.core.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank
        String username,
        @NotBlank
        String password
) {}
