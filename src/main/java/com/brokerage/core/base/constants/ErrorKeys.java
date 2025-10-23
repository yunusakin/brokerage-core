package com.brokerage.core.base.constants;

public final class ErrorKeys {

    private ErrorKeys() {}

    /**
     *  Generic
     */
    public static final String INTERNAL_ERROR = "error.internal";
    public static final String VALIDATION_FAILED = "error.validation.failed";

    /**
     *  Business Logic
     */
    public static final String INSUFFICIENT_BALANCE = "error.business.insufficient_balance";
    public static final String ORDER_NOT_PENDING = "error.business.order_not_pending";

    /**
     *  Resources
     */
    public static final String CUSTOMER_NOT_FOUND = "error.resource.customer_not_found";
    public static final String ASSET_NOT_FOUND = "error.resource.asset_not_found";
    public static final String ORDER_NOT_FOUND = "error.resource.order_not_found";
    public static final String TRY_NOT_FOUND = "error.resource.try_not_found";

    public static final String USER_NOT_FOUND = "error.auth.user_not_found";
    public static final String BAD_CREDENTIALS = "error.auth.bad_credentials";
    public static final String ACCESS_DENIED   = "error.security.access_denied";
}

