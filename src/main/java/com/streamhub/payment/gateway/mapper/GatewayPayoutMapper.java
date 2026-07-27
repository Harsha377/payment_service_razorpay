package com.streamhub.payment.gateway.mapper;

import com.streamhub.payment.beneficiary.entity.FundAccountEntity;
import com.streamhub.payment.gateway.dto.request.GatewayPayoutRequest;
import com.streamhub.payment.payout.dto.request.CreatePayoutRequest;
import com.streamhub.payment.payout.entity.PayoutEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GatewayPayoutMapper {

    @Value("${razorpayx.source-account-number}")
    private String sourceAccountNumber;

    public GatewayPayoutRequest toGatewayPayoutRequest(
            CreatePayoutRequest request,
            FundAccountEntity fundAccount,
            PayoutEntity payout ){
        return GatewayPayoutRequest.builder()
                .accountNumber(sourceAccountNumber)
                .fundAccountId(fundAccount.getGatewayFundAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .mode(request.getMode().name())
                .purpose(request.getPurpose().name().toLowerCase())
                .referenceId(payout.getPayoutReference())
                .queueIfLowBalance(true)
                .notes(request.getMetadata())
                .build();
    }
}
