package com.streamhub.payment.payment.dto.response;

import com.streamhub.payment.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreatePaymentIntentResponse {

    private String paymentIntentReference;

    private String paymentReference;

    private String gatewayOrderId;

    private Long amount;

    private String currency;

    private PaymentStatus status;
    private String gatewayKey;
    private String gatewayName;

}
