package com.brokerage.core.exception;

/**
 * A custom runtime exception representing business rule violations.
 *
 * The message value should always be a message key (from ErrorKeys),
 * which will be localized by the GlobalExceptionHandler.
 */
public class BusinessException extends RuntimeException {

    /**
     * Create a BusinessException using a message key.
     *
     * @param messageKey the key used to look up a localized message
     */
    public BusinessException(String messageKey) {
        super(messageKey);
    }

    /**
     * Create a BusinessException with a message key and a cause.
     *
     * @param messageKey the key used to look up a localized message
     * @param cause the underlying cause of the exception
     */
    public BusinessException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }
}
