package com.streamhub.payment.gateway.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GatewayWebhookEventResponse {

    private String event;

    private String gatewayPaymentId;

    private String gatewayOrderId;

    private String gatewayStatus;

    private String paymentMethod;

    private Long amount;

    private String currency;

    private JsonNode payload;

}