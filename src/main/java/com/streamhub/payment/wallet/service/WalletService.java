package com.streamhub.payment.wallet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.entity.WalletHoldEntity;
import com.streamhub.payment.wallet.enums.WalletOwnerType;
import com.streamhub.payment.wallet.enums.WalletReferenceType;
import com.streamhub.payment.wallet.enums.WalletTransactionSource;

public interface WalletService {

    WalletEntity createWallet(String ownerReference, WalletOwnerType ownerType);

    /** Returns the existing wallet for this owner, or creates one if it does not exist yet. */
    WalletEntity getOrCreateWallet(String ownerReference, WalletOwnerType ownerType);
    void creditWallet(
            WalletEntity wallet,
            Long amount,
            WalletTransactionSource transactionSource,
            WalletReferenceType referenceType,
            String referenceId,
            String description,
            JsonNode metadata );

    WalletEntity  debitWallet(
            WalletEntity wallet,
            Long amount,
            WalletTransactionSource transactionSource,
            WalletReferenceType referenceType,
            String referenceId,
            String description,
            JsonNode metadata );

    WalletHoldEntity createWalletHold(WalletEntity wallet, Long amount);

    WalletHoldEntity attachPayoutToWalletHold(WalletHoldEntity walletHold, PayoutEntity payout);

    void releaseWalletHold(WalletHoldEntity walletHold);

    void cancelWalletHold(WalletHoldEntity walletHold);

    void createPayoutWalletTransaction(WalletHoldEntity walletHold, PayoutEntity payout);

    WalletEntity findWalletByOwner(String ownerReference, WalletOwnerType ownerType);
    void cancelWalletHoldBeforePayout(WalletHoldEntity walletHold);
    WalletHoldEntity findWalletHoldByPayout(PayoutEntity payout);
}
