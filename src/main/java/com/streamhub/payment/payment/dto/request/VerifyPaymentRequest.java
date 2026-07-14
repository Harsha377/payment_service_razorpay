package com.streamhub.payment.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPaymentRequest {

    @NotBlank(message = "Payment reference is required")
    @Size(max = 26,message = "Payment reference must not exceed 26 characters")
    private String paymentReference;

    @NotBlank(message = "Gateway order id is required")
    private String gatewayOrderId;

    @NotBlank(message = "Gateway payment id is required")
    private String gatewayPaymentId;

    @NotBlank(message = "Gateway signature is required")
    private String gatewaySignature;
}
