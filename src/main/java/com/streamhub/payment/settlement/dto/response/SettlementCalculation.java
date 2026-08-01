package com.streamhub.payment.settlement.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SettlementCalculation {
    private   Long grossAmount;

    private Long commissionAmount;

    private Long netAmount;
}
