package com.brokerage.core.base.response;


import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Base class for building standardized API responses.
 * Handles message localization and standard response building.
 */
@RequiredArgsConstructor
public abstract class BaseResponse {

    private final MessageSource messageSource;

    protected String localize(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }

    protected <T> ResponseEntity<Map<String, Object>> ok(String messageKey, T data) {
        return buildResponse(HttpStatus.OK, messageKey, data);
    }

    protected <T> ResponseEntity<Map<String, Object>> created(String messageKey, T data) {
        return buildResponse(HttpStatus.CREATED, messageKey, data);
    }

    protected ResponseEntity<Map<String, Object>> noContent(String messageKey) {
        return buildResponse(HttpStatus.NO_CONTENT, messageKey, null);
    }

    private <T> ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String messageKey, T data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("status", status.value());
        body.put("timestamp", LocalDateTime.now());
        body.put("message", localize(messageKey));
        body.put("data", data);
        return ResponseEntity.status(status).body(body);
    }
}

