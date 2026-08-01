package com.streamhub.payment.settlement.mapper;

import com.streamhub.payment.settlement.dto.response.SettlementResponse;
import com.streamhub.payment.settlement.entity.SettlementEntity;
import org.springframework.stereotype.Component;

@Component
public class SettlementMapper {

    public SettlementResponse toSettlementResponse(SettlementEntity settlement) {

        return SettlementResponse.builder()
                .settlementReference(settlement.getSettlementReference())
                .paymentReference(settlement.getPayment().getPaymentReference())
                .ownerReference(settlement.getOwnerReference())
                .ownerType(settlement.getOwnerType())
                .walletReference(settlement.getWallet().getWalletReference())
                .grossAmount(settlement.getGrossAmount())
                .commissionAmount(settlement.getCommissionAmount())
                .netAmount(settlement.getNetAmount())
                .currency(settlement.getCurrency())
                .status(settlement.getStatus())
                .build();
    }
}