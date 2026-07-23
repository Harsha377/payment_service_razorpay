package com.streamhub.payment.gateway.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.gateway.enums.RazorpayContactType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayContactRequest {
    private String name;
    private String email;
    private String contact;
    /**
     * customer
     * vendor
     * employee
     * self
     */
    private RazorpayContactType type;
    private JsonNode notes;
}
