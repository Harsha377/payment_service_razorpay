package com.streamhub.payment.payment.controller;

import com.streamhub.payment.common.constants.HttpHeadersConstants;
import com.streamhub.payment.payment.dto.request.CreatePaymentIntentRequest;
import com.streamhub.payment.payment.dto.response.CreatePaymentIntentResponse;
import com.streamhub.payment.payment.service.PaymentIntentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment-intents")
@Validated
@CrossOrigin(origins = "*")
public class PaymentIntentController {
    private final PaymentIntentService paymentIntentService;

    @PostMapping
    public ResponseEntity<CreatePaymentIntentResponse> createPaymentIntent(
            @RequestHeader(HttpHeadersConstants.IDEMPOTENCY_KEY) String idempotencyKey,
            @Valid @RequestBody CreatePaymentIntentRequest request
    ) {

        CreatePaymentIntentResponse response =
                paymentIntentService.createPaymentIntent(idempotencyKey,request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
