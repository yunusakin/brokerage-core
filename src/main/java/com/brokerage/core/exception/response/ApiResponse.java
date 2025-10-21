package com.brokerage.core.exception.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        int status,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return new ApiResponse<>(true, message, data, status, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, int status) {
        return new ApiResponse<>(false, message, null, status, LocalDateTime.now());
    }
}

