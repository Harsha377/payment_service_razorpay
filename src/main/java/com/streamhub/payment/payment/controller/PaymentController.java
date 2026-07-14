package com.streamhub.payment.payment.controller;


import com.streamhub.payment.payment.dto.request.VerifyPaymentRequest;
import com.streamhub.payment.payment.dto.response.VerifyPaymentResponse;
import com.streamhub.payment.payment.service.PaymentIntentService;
import com.streamhub.payment.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@Validated
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request
    ) {

        VerifyPaymentResponse response =
                paymentService.verifyPayment(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
