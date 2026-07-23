package com.streamhub.payment.gateway.dto.response;

import com.streamhub.payment.gateway.enums.RazorpayContactType;
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
public class GatewayContactResponse {

    /**
     * Razorpay Contact ID
     */
    private String id;

    /**
     * Contact Name
     */
    private String name;

    /**
     * Contact Email
     */
    private String email;

    /**
     * Contact Mobile Number
     */
    private String contact;

    /**
     * Razorpay Contact Type
     */
    private String  type;

}