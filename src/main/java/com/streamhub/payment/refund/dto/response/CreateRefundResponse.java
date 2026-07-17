package com.streamhub.payment.refund.dto.response;

import com.streamhub.payment.refund.enums.RefundStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateRefundResponse {
    private String refundReference;
    private String paymentReference;
//    private String gatewayRefundId;
    private RefundStatus status;
    private String message;
}
