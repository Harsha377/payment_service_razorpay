package com.streamhub.payment.gateway.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GatewayWebhookVerificationRequest {
    /**
     * Raw JSON payload received from Razorpay.
     * Required for HMAC signature verification.
     */
    private String payload;

    /**
     * Value of the X-Razorpay-Signature header.
     */
    private String signature;
}
