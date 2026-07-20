package com.streamhub.payment.refund.service;

import com.streamhub.payment.gateway.dto.response.GatewayRefundDetailsResponse;
import com.streamhub.payment.refund.dto.request.CreateRefundRequest;
import com.streamhub.payment.refund.dto.response.CreateRefundResponse;
import com.streamhub.payment.refund.dto.response.RefundResponse;
import com.streamhub.payment.refund.dto.response.RefundSummaryResponse;
import com.streamhub.payment.refund.entity.RefundEntity;

import java.util.List;

public interface RefundService {
    CreateRefundResponse createRefund(String idempotencyKey,CreateRefundRequest request);
    RefundResponse getRefund(String refundReference);
    List<RefundSummaryResponse> getRefundsByPayment(String paymentReference);
    RefundEntity completeRefund(RefundEntity refund, GatewayRefundDetailsResponse gatewayDetails);
    RefundEntity failRefund(RefundEntity refund, GatewayRefundDetailsResponse gatewayDetails);
}
