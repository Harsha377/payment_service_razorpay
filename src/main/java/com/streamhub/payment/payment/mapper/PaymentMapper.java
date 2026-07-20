package com.streamhub.payment.payment.mapper;

import com.streamhub.payment.gateway.dto.response.GatewayOrderResponse;
import com.streamhub.payment.payment.dto.response.CreatePaymentIntentResponse;
import com.streamhub.payment.payment.dto.response.PaymentEventResponse;
import com.streamhub.payment.payment.dto.response.PaymentResponse;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.entity.PaymentEventEntity;
import com.streamhub.payment.payment.entity.PaymentIntentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public CreatePaymentIntentResponse toCreatePaymentIntentResponse(
            PaymentIntentEntity paymentIntent, PaymentEntity payment,
            String gatewayKey ) {

        return CreatePaymentIntentResponse.builder()
                .paymentIntentReference(paymentIntent.getPaymentIntentReference())
                .paymentReference(payment.getPaymentReference())
                .gatewayOrderId(payment.getGatewayOrderId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .gatewayKey(gatewayKey)
                .gatewayName(payment.getGatewayName())
                .build();
    }

    public CreatePaymentIntentResponse toCreatePaymentIntentResponse(
            PaymentIntentEntity paymentIntent, PaymentEntity payment,
            GatewayOrderResponse gatewayOrder) {

        return toCreatePaymentIntentResponse(paymentIntent, payment, gatewayOrder.getKeyId());
    }

    public PaymentResponse toPaymentResponse(
            PaymentEntity payment
    ) {

        return PaymentResponse.builder()
                .paymentReference(payment.getPaymentReference())
                .paymentIntentReference(
                        payment.getPaymentIntent().getPaymentIntentReference()
                )
                .gatewayName(payment.getGatewayName())
                .gatewayOrderId(payment.getGatewayOrderId())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }

    public PaymentEventResponse toPaymentEventResponse(
            PaymentEventEntity paymentEvent
    ) {

        return PaymentEventResponse.builder()
                .paymentEventReference(paymentEvent.getPaymentEventReference())
                .eventType(paymentEvent.getEventType())
                .eventSource(paymentEvent.getEventSource())
                .createdAt(paymentEvent.getCreatedAt())
                .build();
    }

}
