package com.streamhub.payment.beneficiary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetDefaultFundAccountResponse {
    private String fundAccountReference;

    private Boolean isDefault;

    private String message;
}
