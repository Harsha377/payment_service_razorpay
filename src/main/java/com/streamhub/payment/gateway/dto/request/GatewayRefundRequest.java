package com.streamhub.payment.gateway.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.refund.enums.RefundSpeed;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayRefundRequest {
    private String gatewayPaymentId;
    private Long amount;
    private RefundSpeed speed;
    private JsonNode notes;
}
