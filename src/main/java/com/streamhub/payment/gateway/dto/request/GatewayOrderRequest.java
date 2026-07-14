package com.streamhub.payment.gateway.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GatewayOrderRequest {
    private String receipt;

    private Long amount;

    private String currency;
}
