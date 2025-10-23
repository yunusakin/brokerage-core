package com.brokerage.core.api.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record AdminCreateRequest(
        @NotBlank
        String username ,
        @NotBlank
        String password
) {}
