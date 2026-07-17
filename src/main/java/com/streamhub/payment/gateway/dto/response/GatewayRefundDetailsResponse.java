package com.streamhub.payment.gateway.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatewayRefundDetailsResponse {
    private String gatewayRefundId;
    private String gatewayStatus;
    private JsonNode gatewayResponse;
    private JsonNode gatewayNotes;
}
