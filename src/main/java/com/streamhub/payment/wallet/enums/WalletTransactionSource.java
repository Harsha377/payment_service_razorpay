package com.streamhub.payment.wallet.enums;

public enum WalletTransactionSource {
    PAYMENT,
    PAYOUT,
    REFUND,
    ADJUSTMENT
}


//        | Source       | Description                                                       |
//        | ------------ | ----------------------------------------------------------------- |
//        | `PAYMENT`    | Earnings credited after a successful customer payment.            |
//        | `PAYOUT`     | Money debited due to a withdrawal.                                |
//        | `REFUND`     | Adjustment related to refunds (depending on your business rules). |
//        | `ADJUSTMENT` | Manual credit/debit by an administrator.                          |
