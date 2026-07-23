package com.streamhub.payment.beneficiary.mapper;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.gateway.dto.request.GatewayFundAccountRequest;
import org.springframework.stereotype.Component;

@Component
public class GatewayFundAccountMapper {
    public GatewayFundAccountRequest toGatewayFundAccountRequest(
            CreateFundAccountRequest request, BeneficiaryEntity beneficiary) {

        GatewayFundAccountRequest gatewayRequest = new GatewayFundAccountRequest();
        gatewayRequest.setContactId(beneficiary.getGatewayContactId());

        switch (request.fundAccountType()) {
            case BANK_ACCOUNT -> {

                gatewayRequest.setAccountType("bank_account");
                gatewayRequest.setBankAccount(
                        GatewayFundAccountRequest.BankAccount.builder()
                                .name(request.accountHolderName())
                                .ifsc(request.ifscCode())
                                .accountNumber(request.accountNumber()).build());
            }

            case VPA -> {

                gatewayRequest.setAccountType("vpa");
                gatewayRequest.setVpa(GatewayFundAccountRequest.Vpa.builder()
                        .address(request.vpa()).build());
            }
        }

        return gatewayRequest;
    }
}
