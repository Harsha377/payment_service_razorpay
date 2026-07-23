package com.streamhub.payment.gateway.dto.request;

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
public class GatewayFundAccountRequest {

    /**
     * Razorpay Contact Id
     */
    private String contactId;

    /**
     * bank_account | vpa
     */
    private String accountType;

    /**
     * Present only for BANK_ACCOUNT
     */
    private BankAccount bankAccount;

    /**
     * Present only for VPA
     */
    private Vpa vpa;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankAccount {

        private String name;

        private String ifsc;

        private String accountNumber;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Vpa {

        /**
         * UPI ID
         */
        private String address;
    }
}