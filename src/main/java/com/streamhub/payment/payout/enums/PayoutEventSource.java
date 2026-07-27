package com.streamhub.payment.payout.enums;

public enum PayoutEventSource {
    API,
    WEBHOOK,
    SYSTEM
}


/*

API → Initial payout creation.
WEBHOOK → Razorpay webhook updates.
SYSTEM → Internal scheduled jobs or reconciliation.

*/