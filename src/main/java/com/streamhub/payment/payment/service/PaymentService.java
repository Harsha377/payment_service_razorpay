package com.streamhub.payment.payment.service;

import com.streamhub.payment.gateway.dto.response.GatewayOrderResponse;
import com.streamhub.payment.gateway.dto.response.GatewayPaymentDetailsResponse;
import com.streamhub.payment.payment.dto.request.VerifyPaymentRequest;
import com.streamhub.payment.payment.dto.response.PaymentResponse;
import com.streamhub.payment.payment.dto.response.VerifyPaymentResponse;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.entity.PaymentIntentEntity;

public interface PaymentService {
    VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request);

    PaymentResponse getPayment(String paymentReference);
    PaymentEntity createPayment(
            PaymentIntentEntity paymentIntent,
            GatewayOrderResponse gatewayOrder
    );
    PaymentEntity  completePayment(
            PaymentEntity payment,
            String gatewayPaymentId,
            String gatewaySignature
    );
    PaymentEntity failPayment(
            PaymentEntity payment,
            String gatewayPaymentId,
            GatewayPaymentDetailsResponse gatewayDetails
    );
}
