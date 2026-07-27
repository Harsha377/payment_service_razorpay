package com.streamhub.payment.gateway.dto.request;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayPayoutRequest {

    /**
     * RazorpayX Source Account Number
     */
    private String accountNumber;

    /**
     * Razorpay Fund Account Id
     */
    private String fundAccountId;

    /**
     * Amount in paise
     */
    private Long amount;

    /**
     * Currency (INR)
     */
    private String currency;

    /**
     * IMPS / NEFT / RTGS / UPI
     */
    private String mode;
    /**
     * payout
     */
    private String purpose;
    /**
     * Platform payout reference (ULID)
     */
    private String referenceId;

    /**
     * Queue payout when source account balance is insufficient
     */
    private Boolean queueIfLowBalance;

    /**
     * Additional business metadata
     */
    private JsonNode notes;
}
