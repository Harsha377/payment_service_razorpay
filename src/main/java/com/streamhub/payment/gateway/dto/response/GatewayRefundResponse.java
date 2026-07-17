package com.streamhub.payment.gateway.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayRefundResponse {
    private String gatewayRefundId;
    private String gatewayStatus;
}
