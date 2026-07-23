package com.streamhub.payment.gateway.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewayFundAccountResponse {

    /**
     * Razorpay Fund Account Id
     */
    private String id;

    /**
     * fund_account
     */
    private String entity;

    /**
     * Razorpay Contact Id
     */
    @JsonProperty("contact_id")
    private String contactId;

    /**
     * bank_account | vpa
     */
    @JsonProperty("account_type")
    private String accountType;

}