package com.streamhub.payment.refund.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.refund.enums.RefundInitiator;
import com.streamhub.payment.refund.enums.RefundReason;
import com.streamhub.payment.refund.enums.RefundSpeed;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateRefundRequest {

    @NotBlank(message = "Payment reference is required.")
    private String paymentReference;

    @NotNull(message = "Refund amount is required.")
    @Min(value = 1,message = "Refund amount must be greater than zero.")
    private Long amount;

    @NotNull(message = "Refund reason is required.")
    private RefundReason reason;

    @NotNull(message = "Refund speed is required.")
    private RefundSpeed speed;

    @NotNull(message = "Refund initiator is required.")
    private RefundInitiator initiator;

    private JsonNode metadata;
}
