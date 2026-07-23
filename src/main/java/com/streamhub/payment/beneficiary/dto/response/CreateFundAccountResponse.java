package com.streamhub.payment.beneficiary.dto.response;

import com.streamhub.payment.beneficiary.enums.FundAccountStatus;
import com.streamhub.payment.beneficiary.enums.FundAccountType;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record CreateFundAccountResponse(
    String beneficiaryReference,
    String fundAccountReference,
    FundAccountType fundAccountType,
    FundAccountStatus status,
    Boolean isDefault,
    OffsetDateTime createdAt
    )
{

}
