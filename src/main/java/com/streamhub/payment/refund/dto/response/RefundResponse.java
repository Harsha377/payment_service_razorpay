package com.streamhub.payment.refund.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.refund.enums.RefundInitiator;
import com.streamhub.payment.refund.enums.RefundReason;
import com.streamhub.payment.refund.enums.RefundSpeed;
import com.streamhub.payment.refund.enums.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class RefundResponse {
    private String refundReference;
    private String paymentReference;
    private String gatewayRefundId;
    private Long amount;
    private String currency;
    private RefundReason reason;
    private RefundSpeed speed;
    private RefundInitiator initiator;
    private RefundStatus status;
    private String gatewayStatus;
    private JsonNode metadata;
    private JsonNode gatewayResponse;
    private JsonNode gatewayNotes;
    private OffsetDateTime processedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
