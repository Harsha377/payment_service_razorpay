package com.streamhub.payment.settlement.dto.response;

import com.streamhub.payment.settlement.enums.SettlementStatus;
import com.streamhub.payment.wallet.enums.WalletOwnerType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SettlementResponse {

    String settlementReference;

    String paymentReference;

    String ownerReference;

    WalletOwnerType ownerType;

    String walletReference;

    Long grossAmount;

    Long commissionAmount;

    Long netAmount;

    String currency;

    SettlementStatus status;
}