package com.brokerage.core.base.constants;


public final class SuccessKeys {

    private SuccessKeys() {}

    /**
     *  Generic
     */
    public static final String GENERIC_SUCCESS = "success.generic";

    /**
     *  Customer
     */
    public static final String CUSTOMER_CREATED = "success.customer.created";

    /**
     *  Order
     */
    public static final String ORDER_CREATED = "success.order.created";
    public static final String ORDER_CANCELED = "success.order.canceled";
    public static final String ORDER_MATCHED = "success.order.matched";

    /**
     *  Asset
     */
    public static final String ASSET_FETCHED = "success.asset.fetched";

    /**
     *  Authentication
     */
    public static final String USER_REGISTERED = "success.user.registered";
    public static final String USER_LOGGED_IN  = "success.user.logged_in";
}
