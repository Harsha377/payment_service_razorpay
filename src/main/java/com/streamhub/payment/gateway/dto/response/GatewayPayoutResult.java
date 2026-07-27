package com.streamhub.payment.gateway.dto.response;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GatewayPayoutResult {

    /**
     * Parsed gateway response
     */
    private GatewayPayoutResponse response;

    /**
     * Complete raw Razorpay response
     */
    private JsonNode gatewayResponse;

}
