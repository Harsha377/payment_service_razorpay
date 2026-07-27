package com.streamhub.payment.payout.enums;

public enum PayoutEventType {

    PAYOUT_CREATED,
    PAYOUT_PENDING,
    PAYOUT_PROCESSING,
    PAYOUT_PROCESSED,
    PAYOUT_FAILED,
    PAYOUT_CANCELLED,
    PAYOUT_REVERSED
}


//Every status transition should create a new event.

/*
PAYOUT_CREATED

↓

PAYOUT_PENDING

↓

PAYOUT_PROCESSING

↓

PAYOUT_PROCESSED
*/