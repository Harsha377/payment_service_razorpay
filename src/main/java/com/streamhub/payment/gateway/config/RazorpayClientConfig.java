package com.streamhub.payment.gateway.config;

import com.razorpay.RazorpayClient;
import com.streamhub.payment.gateway.properties.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RazorpayClientConfig {
    private final RazorpayProperties razorpayProperties;
    @Bean
    public RazorpayClient razorpayClient() throws Exception{
        return new RazorpayClient(
                razorpayProperties.getKeyId(),
                razorpayProperties.getKeySecret()
        );
    }
}
