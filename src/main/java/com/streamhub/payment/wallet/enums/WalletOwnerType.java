package com.streamhub.payment.wallet.enums;

/**
 * Identifies the kind of entity that owns a wallet.
 * <p>
 * A wallet is now identified purely by (ownerReference, ownerType) and has
 * no structural dependency on {@code BeneficiaryEntity}. A Beneficiary
 * (Razorpay contact + fund account) is only required later, at the moment
 * an actual bank payout is initiated for a PROVIDER-owned wallet.
 */
public enum WalletOwnerType {

    /** Wallet belongs to a customer, e.g. holds refund credits. */
    CUSTOMER,

    /** Wallet belongs to a provider/merchant/nutritionist receiving payin settlements. */
    PROVIDER,

    /** Wallet belongs to the platform itself, e.g. commission collection. */
    PLATFORM
}
