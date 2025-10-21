package com.brokerage.core.constants;

public final class ErrorKeys {

    private ErrorKeys() {}

    // ---- Generic Errors ----
    public static final String INTERNAL_ERROR = "error.internal";
    public static final String VALIDATION_FAILED = "error.validation.failed";

    // ---- Business Errors ----
    public static final String INSUFFICIENT_BALANCE = "error.business.insufficient_balance";
    public static final String ASSET_NOT_FOUND = "error.business.asset_not_found";
    public static final String TRY_NOT_FOUND = "error.business.try_not_found";
    public static final String ORDER_NOT_PENDING = "error.business.order_not_pending";

    // ---- Success / Generic Messages ----
    public static final String SUCCESS_GENERIC = "success.generic";
}

