package com.streamhub.payment.wallet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.entity.WalletHoldEntity;
import com.streamhub.payment.wallet.enums.WalletReferenceType;
import com.streamhub.payment.wallet.enums.WalletTransactionSource;

public interface WalletService {

    WalletEntity createWallet(BeneficiaryEntity beneficiary);
    WalletEntity  creditWallet(
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

    WalletEntity findWalletByBeneficiary(BeneficiaryEntity beneficiary);
    void cancelWalletHoldBeforePayout(WalletHoldEntity walletHold);
    WalletHoldEntity findWalletHoldByPayout(PayoutEntity payout);
}
