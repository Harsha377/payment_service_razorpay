package com.streamhub.payment.wallet.enums;

public enum WalletStatus {
    ACTIVE,
    INACTIVE,
    BLOCKED
}


//Wallet can receive credits and process withdrawals.   - ACTIVE
//Wallet exists but cannot be used yet (e.g., beneficiary onboarding incomplete).   - INACTIVE
//Wallet operations are blocked due to compliance, fraud, or administrative action.  - BLOCKED