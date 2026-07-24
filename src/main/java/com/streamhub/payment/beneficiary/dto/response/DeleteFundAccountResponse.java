package com.streamhub.payment.beneficiary.dto.response;

import com.streamhub.payment.beneficiary.enums.FundAccountStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteFundAccountResponse {
    private String fundAccountReference;

    private FundAccountStatus status;

    private String message;

}
