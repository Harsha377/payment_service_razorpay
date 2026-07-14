package com.streamhub.payment.gateway.service;

import com.streamhub.payment.gateway.dto.request.GatewayOrderRequest;
import com.streamhub.payment.gateway.dto.request.GatewayPaymentVerificationRequest;
import com.streamhub.payment.gateway.dto.response.GatewayOrderResponse;
import com.streamhub.payment.gateway.dto.response.GatewayPaymentDetailsResponse;


public interface RazorpayService {

    GatewayOrderResponse createOrder(GatewayOrderRequest request);
    void verifyPayment(GatewayPaymentVerificationRequest request);
    GatewayPaymentDetailsResponse fetchPayment(String gatewayPaymentId);
}


//It only knows how to communicate with an external provider.