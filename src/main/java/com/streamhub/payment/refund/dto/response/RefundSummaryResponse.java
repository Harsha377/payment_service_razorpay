package com.streamhub.payment.refund.dto.response;

import com.streamhub.payment.refund.enums.RefundInitiator;
import com.streamhub.payment.refund.enums.RefundReason;
import com.streamhub.payment.refund.enums.RefundSpeed;
import com.streamhub.payment.refund.enums.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class RefundSummaryResponse {

    private String refundReference;
    private String paymentReference;
    private Long amount;
    private String currency;
    private RefundReason reason;
    private RefundSpeed speed;
    private RefundInitiator initiator;
    private RefundStatus status;
    private OffsetDateTime processedAt;
    private OffsetDateTime createdAt;
}
