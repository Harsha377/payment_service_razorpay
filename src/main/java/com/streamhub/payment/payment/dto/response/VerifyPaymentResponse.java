package com.streamhub.payment.payment.dto.response;

import com.streamhub.payment.payment.enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyPaymentResponse {

    private String paymentReference;

    private String paymentIntentReference;

    private String gatewayPaymentId;

    private PaymentStatus status;

    private String message;

}