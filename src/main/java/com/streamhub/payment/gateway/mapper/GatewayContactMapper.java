package com.streamhub.payment.gateway.mapper;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.enums.OwnerType;
import com.streamhub.payment.gateway.dto.request.GatewayContactRequest;
import com.streamhub.payment.gateway.enums.RazorpayContactType;
import org.springframework.stereotype.Component;

@Component
public class GatewayContactMapper  {

    public RazorpayContactType mapContactType(OwnerType ownerType){
        return switch (ownerType){
            case USER -> RazorpayContactType.CUSTOMER;
            case MERCHANT, VENDOR,PARTNER -> RazorpayContactType.VENDOR;
            case EMPLOYEE -> RazorpayContactType.EMPLOYEE;

        };
    }
    public GatewayContactRequest toGatewayContactRequest(CreateFundAccountRequest request){
        return GatewayContactRequest.builder()
                .name(request.name())
                .email(request.email())
                .contact(request.contactNumber())
                .type(mapContactType(request.ownerType()))
                .notes(request.metadata())
                .build();
    }
}
