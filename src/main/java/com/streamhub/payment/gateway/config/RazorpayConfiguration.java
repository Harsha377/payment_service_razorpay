package com.streamhub.payment.gateway.config;

import com.streamhub.payment.gateway.config.properties.RazorpayProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RazorpayProperties.class)
public class RazorpayConfiguration {
}
