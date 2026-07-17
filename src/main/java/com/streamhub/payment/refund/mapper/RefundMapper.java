package com.streamhub.payment.refund.mapper;

import com.streamhub.payment.refund.dto.response.CreateRefundResponse;
import com.streamhub.payment.refund.dto.response.RefundResponse;
import com.streamhub.payment.refund.dto.response.RefundSummaryResponse;
import com.streamhub.payment.refund.entity.RefundEntity;
import org.springframework.stereotype.Component;

@Component
public class RefundMapper {
    public CreateRefundResponse toCreateRefundResponse(RefundEntity refund){

        return CreateRefundResponse.builder()
                .refundReference(refund.getRefundReference())
                .paymentReference(refund.getPayment().getPaymentReference())
                .status(refund.getStatus())
                .message("Refund created successfully.")
                .build();
    }

    public RefundResponse toRefundResponse(RefundEntity refund){

        return RefundResponse.builder()
                .refundReference(refund.getRefundReference())
                .paymentReference(refund.getPayment().getPaymentReference())
                .gatewayRefundId(refund.getGatewayRefundId())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .reason(refund.getReason())
                .speed(refund.getSpeed())
                .initiator(refund.getInitiator())
                .status(refund.getStatus())
                .gatewayStatus(refund.getGatewayStatus())
                .metadata(refund.getMetadata())
                .gatewayResponse(refund.getGatewayResponse())
                .gatewayNotes(refund.getGatewayNotes())
                .processedAt(refund.getProcessedAt())
                .createdAt(refund.getCreatedAt())
                .updatedAt(refund.getUpdatedAt())
                .build();
    }

    public RefundSummaryResponse toRefundSummaryResponse(RefundEntity refund){

        return RefundSummaryResponse.builder()
                .refundReference(refund.getRefundReference())
                .paymentReference(refund.getPayment().getPaymentReference())
                .amount(refund.getAmount())
                .currency(refund.getCurrency())
                .reason(refund.getReason())
                .speed(refund.getSpeed())
                .initiator(refund.getInitiator())
                .status(refund.getStatus())
                .processedAt(refund.getProcessedAt())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
