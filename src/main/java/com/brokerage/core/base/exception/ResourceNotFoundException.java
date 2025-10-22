package com.brokerage.core.base.exception;


/**
 * Exception thrown when a requested resource cannot be found in the system.
 * The message should be a message key from ErrorKeys (localized automatically).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String messageKey) {
        super(messageKey);
    }

    public ResourceNotFoundException(String messageKey, Throwable cause) {
        super(messageKey, cause);
    }
}
