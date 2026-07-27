package com.streamhub.payment.payout.controller;


import com.streamhub.payment.common.constants.HttpHeadersConstants;
import com.streamhub.payment.gateway.dto.response.GatewayPayoutDetailsResponse;
import com.streamhub.payment.payout.dto.request.CreatePayoutRequest;
import com.streamhub.payment.payout.dto.response.CreatePayoutResponse;
import com.streamhub.payment.payout.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payouts")
public class PayoutController {

    private final PayoutService payoutService;

    @PostMapping
    public ResponseEntity<CreatePayoutResponse> createPayout(
            @RequestHeader(HttpHeadersConstants.IDEMPOTENCY_KEY) String idempotencyKey,
            @Valid @RequestBody CreatePayoutRequest request
            ){
        CreatePayoutResponse response =
                payoutService.createPayout(request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

//    fetches the latest state directly from Razorpay.
    @GetMapping("/{gatewayPayoutId}/gateway")
    public ResponseEntity<GatewayPayoutDetailsResponse> getGatewayPayout(
            @PathVariable String gatewayPayoutId) {

        GatewayPayoutDetailsResponse response =
                payoutService.getGatewayPayout(gatewayPayoutId);

        return ResponseEntity.ok(response);
    }
}
