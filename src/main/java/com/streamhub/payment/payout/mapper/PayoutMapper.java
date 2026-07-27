package com.streamhub.payment.payout.mapper;

import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.entity.FundAccountEntity;
import com.streamhub.payment.payment.enums.PaymentGateway;
import com.streamhub.payment.payout.dto.request.CreatePayoutRequest;
import com.streamhub.payment.payout.dto.response.CreatePayoutResponse;
import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.payout.enums.PayoutStatus;
import org.springframework.stereotype.Component;

@Component
public class PayoutMapper {

    public PayoutEntity toPayoutEntity(
            CreatePayoutRequest request,
            BeneficiaryEntity beneficiary,
            FundAccountEntity fundAccount){
        return PayoutEntity.builder()
                .beneficiary(beneficiary)
                .fundAccount(fundAccount)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .mode(request.getMode())
                .purpose(request.getPurpose())
                .metadata(request.getMetadata())
                .gatewayName(PaymentGateway.RAZORPAY)
                .status(PayoutStatus.CREATED)
                .build();
    }

    public CreatePayoutResponse toCreatePayoutResponse(PayoutEntity payout){
        return CreatePayoutResponse.builder()
                .payoutReference(payout.getPayoutReference())
                .beneficiaryReference(payout.getBeneficiary().getBeneficiaryReference())
                .fundAccountReference(payout.getFundAccount().getFundAccountReference())
                .amount(payout.getAmount())
                .currency(payout.getCurrency())
                .status(payout.getStatus())
                .build();
    }
}
