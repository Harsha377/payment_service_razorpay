package com.streamhub.payment.gateway.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GatewayPaymentVerificationRequest {

    private String gatewayOrderId;

    private String gatewayPaymentId;

    private String gatewaySignature;

}
