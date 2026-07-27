package com.streamhub.payment.payout.dto.response;

import com.streamhub.payment.payout.enums.PayoutStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayoutResponse {

    private String payoutReference;
    private String beneficiaryReference;
    private String fundAccountReference;
    private Long amount;
    private String currency;
    private PayoutStatus status;
}