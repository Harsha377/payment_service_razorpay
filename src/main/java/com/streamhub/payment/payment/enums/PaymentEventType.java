package com.streamhub.payment.payment.enums;

public enum PaymentEventType {

    PAYMENT_INTENT_CREATED,

    PAYMENT_CREATED,

    PAYMENT_AUTHORIZED,

    PAYMENT_CAPTURED,

    GATEWAY_ORDER_CREATED,

    PAYMENT_PENDING,

    PAYMENT_SUCCESS,

    PAYMENT_FAILED,

    PAYMENT_EXPIRED

}