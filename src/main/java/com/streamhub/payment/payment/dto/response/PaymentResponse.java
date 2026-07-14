package com.streamhub.payment.payment.dto.response;

import com.streamhub.payment.payment.enums.PaymentGateway;
import com.streamhub.payment.payment.enums.PaymentMethod;
import com.streamhub.payment.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PaymentResponse {

    private String paymentReference;

    private String paymentIntentReference;

    private PaymentGateway gatewayName;

    private String gatewayOrderId;

    private String gatewayPaymentId;

    private Long amount;

    private String currency;

    private PaymentMethod paymentMethod;

    private PaymentStatus status;

    private OffsetDateTime paidAt;

}
