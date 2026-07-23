package com.streamhub.payment.gateway.config;

import com.razorpay.RazorpayClient;
import com.streamhub.payment.gateway.config.properties.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;


import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

    @Bean
    public RestClient razorpayXRestClient(){
        String credentials=
                razorpayProperties.getKeyId()+":"+razorpayProperties.getKeySecret();
        String basicAuth=Base64.getEncoder()
                .encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );
        return RestClient.builder()
                .baseUrl(razorpayProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION,"Basic " + basicAuth)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }
}
