package com.streamhub.payment.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewayPayoutDetailsResponse {
    /**
     * Razorpay Payout Id
     */
    private String id;

    /**
     * queued | pending | processing | processed | failed | cancelled | reversed
     */
    private String status;

    /**
     * IMPS | NEFT | RTGS | UPI
     */
    private String mode;

    /**
     * payout
     */
    private String purpose;

    /**
     * Amount in smallest currency unit.
     */
    private Long amount;

    /**
     * INR
     */
    private String currency;

    /**
     * Razorpay Fund Account Id
     */
    @JsonProperty("fund_account_id")
    private String fundAccountId;

    /**
     * Reference Id sent by our application.
     */
    @JsonProperty("reference_id")
    private String referenceId;

    /**
     * Failure reason, if any.
     */
    @JsonProperty("status_details")
    private JsonNode statusDetails;
}
