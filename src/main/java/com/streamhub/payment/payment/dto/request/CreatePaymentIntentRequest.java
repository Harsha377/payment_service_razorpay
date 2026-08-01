package com.streamhub.payment.payment.dto.request;

import com.streamhub.payment.payment.enums.PaymentFlowType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentIntentRequest {
    @NotBlank(message = "Order reference is required")
    @Size(max = 26, message = "Order reference must not exceed 26 characters")
    private String orderReference;

    @NotBlank(message = "Customer reference is required")
    @Size(max = 26, message ="Customer reference must not exceed 26 characters" )
    private String customerReference;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private Long amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO currency code")
    private String currency;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "payment flow type is required.")
    private PaymentFlowType paymentFlowType;

    private String settlementBeneficiaryReference;

}
