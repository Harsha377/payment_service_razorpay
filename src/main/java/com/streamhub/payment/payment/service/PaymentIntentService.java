package com.streamhub.payment.payment.service;

import com.streamhub.payment.payment.dto.request.CreatePaymentIntentRequest;
import com.streamhub.payment.payment.dto.response.CreatePaymentIntentResponse;

public interface PaymentIntentService {
    CreatePaymentIntentResponse createPaymentIntent(String idempotencyKey,CreatePaymentIntentRequest request);
}
