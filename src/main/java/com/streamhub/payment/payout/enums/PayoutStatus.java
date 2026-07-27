package com.streamhub.payment.payout.enums;

public enum  PayoutStatus {
    CREATED,
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED,
    CANCELLED,
    REVERSED
}


//Lifecycle

/*
CREATED
    ↓
PENDING
    ↓
PROCESSING
    ↓
PROCESSED
 */



//Failure

/*
CREATED
    ↓
FAILED
 */

//Reversal

/*
PROCESSED
    ↓
REVERSED
*/
