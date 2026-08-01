package com.streamhub.payment.settlement.service;

import com.streamhub.payment.payment.entity.PaymentEntity;

public interface SettlementService {

    /**
     * Percentage-based settlement: platform commission is a fixed % of the
     * payment amount (see PLATFORM_COMMISSION_PERCENT), the rest is credited
     * to the owner's wallet.
     *
     * @param ownerReference external reference of the wallet owner receiving
     *                        the payout share (e.g. a provider's beneficiaryReference)
     */
    void createSettlement(PaymentEntity payment, String ownerReference);

    /**
     * Fixed-amount split settlement, e.g. customer pays 1200: 1000 credited to
     * the provider's wallet (netAmount) + 200 platform commission (commissionAmount).
     * netAmount + commissionAmount must equal payment.getAmount().
     *
     * @param ownerReference external reference of the wallet owner receiving netAmount
     */
    void createSettlement(PaymentEntity payment, String ownerReference, Long netAmount, Long commissionAmount);
}
