package com.streamhub.payment.payment.dto.response;

import com.streamhub.payment.payment.enums.PaymentEventSource;
import com.streamhub.payment.payment.enums.PaymentEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PaymentEventResponse {

    private String paymentEventReference;

    private PaymentEventType eventType;

    private PaymentEventSource eventSource;

    private String eventData;

    private OffsetDateTime createdAt;

}
