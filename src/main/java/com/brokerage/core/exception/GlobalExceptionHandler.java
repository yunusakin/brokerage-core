package com.brokerage.core.exception;

import com.brokerage.core.constants.ErrorKeys;
import com.brokerage.core.exception.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Resolve a message key to a localized string.
     */
    private String localize(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }

    // ----------------------------------------------------------------------
    // BusinessException
    // ----------------------------------------------------------------------
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        String message = localize(ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(message, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    // ----------------------------------------------------------------------
    // ResourceNotFoundException
    // ----------------------------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        String message = localize(ex.getMessage());
        ApiResponse<Void> response = ApiResponse.error(message, HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ----------------------------------------------------------------------
    // Validation Errors (@Valid)
    // ----------------------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String fieldError = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        String message = localize(ErrorKeys.VALIDATION_FAILED, fieldError);
        ApiResponse<Void> response = ApiResponse.error(message, HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(response);
    }

    // ----------------------------------------------------------------------
    // Generic / Unexpected Exceptions
    // ----------------------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        // Optional: log unexpected errors here
        String message = localize(ErrorKeys.INTERNAL_ERROR);
        ApiResponse<Void> response = ApiResponse.error(message, HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.internalServerError().body(response);
    }
}
