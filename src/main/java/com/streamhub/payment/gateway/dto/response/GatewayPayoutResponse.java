package com.streamhub.payment.gateway.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewayPayoutResponse {

    /**
     * Razorpay Payout Id
     */
    private String id;

    /**
     * payout
     */
    private String entity;

    /**
     * Razorpay Fund Account Id
     */
    @JsonProperty("fund_account_id")
    private String fundAccountId;

    /**
     * Amount in paise
     */
    private Long amount;

    /**
     * Currency
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
     * queued / pending / processing / processed / failed / cancelled / reversed
     */
    private String status;


    /**
     * Platform payout reference
     */
    @JsonProperty("reference_id")
    private String referenceId;

}
