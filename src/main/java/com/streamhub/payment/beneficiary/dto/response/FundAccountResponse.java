package com.streamhub.payment.beneficiary.dto.response;

import com.streamhub.payment.beneficiary.enums.FundAccountStatus;
import com.streamhub.payment.beneficiary.enums.FundAccountType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundAccountResponse {

    private String fundAccountReference;
    private FundAccountType fundAccountType;
    private String accountHolderName;
    /**
     * Masked account number
     * Example: ********1234
     */
    private String accountNumber;
    private String ifscCode;
    private String vpa;
    private Boolean isDefault;
    private FundAccountStatus status;
}
