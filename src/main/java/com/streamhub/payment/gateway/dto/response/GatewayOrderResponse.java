package com.streamhub.payment.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GatewayOrderResponse {
    private String gatewayOrderId;

    private String gatewayName;

    private String status;

    private String keyId;
}
