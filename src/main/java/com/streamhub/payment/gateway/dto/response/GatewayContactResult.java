package com.streamhub.payment.gateway.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayContactResult {
    /**
     * Typed response used by business logic.
     */
    private GatewayContactResponse response;

    /**
     * Complete response returned by Razorpay.
     * Stored in gateway_response column.
     */
    private JsonNode gatewayResponse;
}
