package com.streamhub.payment.gateway.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.payment.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class GatewayPaymentDetailsResponse {
    private PaymentMethod paymentMethod;
    private String gatewayStatus;
    private JsonNode gatewayResponse;
}
