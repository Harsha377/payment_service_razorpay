package com.streamhub.payment.webhook.service;

import com.streamhub.payment.gateway.dto.request.GatewayWebhookVerificationRequest;
import com.streamhub.payment.gateway.dto.response.GatewayWebhookEventResponse;

public interface WebhookService {

    void processWebhook(GatewayWebhookVerificationRequest request);
    GatewayWebhookEventResponse parseWebhook(
            String payload
    );
}
