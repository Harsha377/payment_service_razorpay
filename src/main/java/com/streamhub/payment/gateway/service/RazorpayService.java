package com.streamhub.payment.gateway.service;

import com.streamhub.payment.gateway.dto.request.*;
import com.streamhub.payment.gateway.dto.response.*;


public interface RazorpayService {

    GatewayOrderResponse createOrder(GatewayOrderRequest request);
    void verifyPayment(GatewayPaymentVerificationRequest request);
    GatewayPaymentDetailsResponse fetchPayment(String gatewayPaymentId);
    void verifyWebhookSignature(
            GatewayWebhookVerificationRequest request
    );
    GatewayRefundResponse createRefund(GatewayRefundRequest request);
    GatewayRefundDetailsResponse fetchRefund(String gatewayRefundId);

    GatewayContactResult  createContact(GatewayContactRequest request);
    GatewayFundAccountResult createFundAccount(GatewayFundAccountRequest request);

}


//It only knows how to communicate with an external provider.