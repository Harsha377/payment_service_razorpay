package com.streamhub.payment.payment.service;

import com.streamhub.payment.payment.dto.response.PaymentEventResponse;
import com.streamhub.payment.payment.entity.PaymentEntity;

import java.util.List;

public interface PaymentEventService {
    void recordPaymentCreated(PaymentEntity payment);
    List<PaymentEventResponse> getPaymentEvents(String paymentReference);
    void recordPaymentSuccess(PaymentEntity payment);

    void recordPaymentFailed(PaymentEntity payment);

    void recordPaymentExpired(PaymentEntity payment);

    void recordPaymentAuthorized(PaymentEntity payment);
}
