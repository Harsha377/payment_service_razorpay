package com.streamhub.payment.payout.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.payout.enums.PayoutMode;
import com.streamhub.payment.payout.enums.PayoutPurpose;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayoutRequest {

    @NotBlank(message = "Beneficiary reference is required.")
    private String beneficiaryReference;

    @NotNull(message = "Amount is required.")
    @Min(value = 1, message = "Amount must be greater than zero.")
    private Long amount;

    @NotBlank(message = "Currency is required.")
    @Size(min = 3, max = 3, message = "Currency must be a valid 3-letter ISO code.")
    private String currency;

    @NotNull(message = "Payout mode is required.")
    private PayoutMode mode;

    @NotNull(message = "Payout purpose is required.")
    private PayoutPurpose purpose;

    private JsonNode metadata;
}