package com.streamhub.payment.beneficiary.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.beneficiary.enums.BeneficiaryStatus;
import com.streamhub.payment.beneficiary.enums.OwnerType;
import com.streamhub.payment.payment.enums.PaymentGateway;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryResponse {
    private String beneficiaryReference;
    private String ownerReference;
    private OwnerType ownerType;
    private String name;
    private String email;
    private String contactNumber;
    private PaymentGateway gatewayName;
    private String gatewayContactId;
    private BeneficiaryStatus status;
    private JsonNode metadata;
}
