package com.streamhub.payment.wallet.enums;

public enum WalletHoldStatus {
    ACTIVE,
    RELEASED,
    CANCELLED,
    EXPIRED
}


//        | Status      | Description                                          |
//        | ----------- | ---------------------------------------------------- |
//        | `ACTIVE`    | Money is currently reserved.                         |
//        | `RELEASED`  | Hold completed successfully after payout.            |
//        | `CANCELLED` | Hold removed because payout failed or was cancelled. |
//        | `EXPIRED`   | Hold automatically expired due to timeout.           |
