package com.streamhub.payment.gateway.service;

import com.streamhub.payment.gateway.dto.request.GatewayOrderRequest;
import com.streamhub.payment.gateway.dto.request.GatewayPaymentVerificationRequest;
import com.streamhub.payment.gateway.dto.request.GatewayRefundRequest;
import com.streamhub.payment.gateway.dto.request.GatewayWebhookVerificationRequest;
import com.streamhub.payment.gateway.dto.response.GatewayOrderResponse;
import com.streamhub.payment.gateway.dto.response.GatewayPaymentDetailsResponse;
import com.streamhub.payment.gateway.dto.response.GatewayRefundDetailsResponse;
import com.streamhub.payment.gateway.dto.response.GatewayRefundResponse;


public interface RazorpayService {

    GatewayOrderResponse createOrder(GatewayOrderRequest request);
    void verifyPayment(GatewayPaymentVerificationRequest request);
    GatewayPaymentDetailsResponse fetchPayment(String gatewayPaymentId);
    void verifyWebhookSignature(
            GatewayWebhookVerificationRequest request
    );
    GatewayRefundResponse createRefund(GatewayRefundRequest request);
    GatewayRefundDetailsResponse fetchRefund(String gatewayRefundId);

}


//It only knows how to communicate with an external provider.