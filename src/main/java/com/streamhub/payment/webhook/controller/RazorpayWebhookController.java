package com.streamhub.payment.webhook.controller;


import com.streamhub.payment.common.constants.HttpHeadersConstants;
import com.streamhub.payment.gateway.dto.request.GatewayWebhookVerificationRequest;
import com.streamhub.payment.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhooks")
@CrossOrigin(origins = "*")
public class RazorpayWebhookController {

    private final WebhookService webhookService;
    @PostMapping("/razorpay")
    public ResponseEntity<Void> receiveWebhook(@RequestHeader(HttpHeadersConstants.RAZORPAY_SIGNATURE) String signature, @RequestBody String payload){
        GatewayWebhookVerificationRequest request =
                GatewayWebhookVerificationRequest.builder()
                        .signature(signature)
                        .payload(payload)
                        .build();
        webhookService.processWebhook(request);
        return ResponseEntity.ok().build();
    }
}
