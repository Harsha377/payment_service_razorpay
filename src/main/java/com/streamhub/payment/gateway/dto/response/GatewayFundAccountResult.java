package com.streamhub.payment.gateway.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayFundAccountResult {

    /**
     * Typed response used by business logic.
     */
    private GatewayFundAccountResponse response;

    /**
     * Complete Razorpay response.
     * Stored in fund_accounts.gateway_response.
     */
    private JsonNode gatewayResponse;

}