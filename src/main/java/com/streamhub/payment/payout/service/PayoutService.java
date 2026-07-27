package com.streamhub.payment.payout.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.gateway.dto.response.GatewayPayoutDetailsResponse;
import com.streamhub.payment.payout.dto.request.CreatePayoutRequest;
import com.streamhub.payment.payout.dto.response.CreatePayoutResponse;
import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.payout.enums.PayoutEventType;
import com.streamhub.payment.payout.enums.PayoutStatus;

public interface PayoutService {

    CreatePayoutResponse createPayout(CreatePayoutRequest request,String idempotencyKey);

    GatewayPayoutDetailsResponse getGatewayPayout(String gatewayPayoutId);

    void updatePayoutFromWebhook(
            PayoutEntity payout, PayoutStatus status,
            PayoutEventType eventType, JsonNode webhookPayload, JsonNode payoutNode);
}
