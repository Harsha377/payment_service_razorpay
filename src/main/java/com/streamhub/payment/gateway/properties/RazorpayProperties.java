package com.streamhub.payment.gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "razorpay")
public class RazorpayProperties {
    private String keyId;
    private String keySecret;
    private String webhookSecret;
}
