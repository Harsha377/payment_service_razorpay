package com.streamhub.payment.settlement.enums;

public enum SettlementStatus {

    CREATED,
    PROCESSING,
    COMPLETED,
    FAILED
}


/*
| Status       | Description                                                                                      |
| ------------ | ------------------------------------------------------------------------------------------------ |
| `CREATED`    | Settlement record has been created but processing has not started.                               |
| `PROCESSING` | Business rules are being applied and the wallet is being credited.                               |
| `COMPLETED`  | Wallet has been successfully credited and settlement is finished.                                |
| `FAILED`     | Settlement processing failed. No wallet changes should remain if the transaction is rolled back. |

* */