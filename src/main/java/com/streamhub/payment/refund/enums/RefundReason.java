package com.streamhub.payment.refund.enums;

public enum RefundReason {
    CUSTOMER_REQUEST,
    MERCHANT_REQUEST,
    ORDER_CANCELLED,
    DUPLICATE_PAYMENT,
    FRAUD,
    SERVICE_UNAVAILABLE,
    OTHER
}
