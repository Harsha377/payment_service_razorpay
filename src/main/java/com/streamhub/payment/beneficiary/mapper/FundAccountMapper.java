package com.streamhub.payment.beneficiary.mapper;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.dto.response.BeneficiaryResponse;
import com.streamhub.payment.beneficiary.dto.response.CreateFundAccountResponse;
import com.streamhub.payment.beneficiary.dto.response.FundAccountResponse;
import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.entity.FundAccountEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class FundAccountMapper {

    public BeneficiaryEntity toBeneficiaryEntity(CreateFundAccountRequest request) {
        BeneficiaryEntity beneficiary = new BeneficiaryEntity();
        beneficiary.setOwnerReference(request.ownerReference());
        beneficiary.setOwnerType(request.ownerType());
        beneficiary.setName(request.name());
        beneficiary.setEmail(request.email());
        beneficiary.setContactNumber(request.contactNumber());
        beneficiary.setMetadata(request.metadata());
        return beneficiary;
    }

    public FundAccountEntity toFundAccountEntity(CreateFundAccountRequest request) {
        FundAccountEntity fundAccount = new FundAccountEntity();
        fundAccount.setFundAccountType(request.fundAccountType());
        fundAccount.setAccountHolderName(request.accountHolderName());
        fundAccount.setAccountNumber(request.accountNumber());
        fundAccount.setIfscCode(request.ifscCode());
        fundAccount.setVpa(request.vpa());
        fundAccount.setIsDefault(request.isDefault());
        fundAccount.setMetadata(request.metadata());

        return fundAccount;
    }

    public CreateFundAccountResponse toCreateFundAccountResponse(
            BeneficiaryEntity beneficiary, FundAccountEntity fundAccount
    ) {
        return CreateFundAccountResponse.builder()
                .beneficiaryReference(beneficiary.getBeneficiaryReference())
                .fundAccountReference(fundAccount.getFundAccountReference())
                .fundAccountType(fundAccount.getFundAccountType())
                .status(fundAccount.getStatus())
                .isDefault(fundAccount.getIsDefault())
                .createdAt(fundAccount.getCreatedAt())
                .build();
    }

    public BeneficiaryResponse toBeneficiaryResponse(BeneficiaryEntity entity){
        return BeneficiaryResponse.builder()
                .beneficiaryReference(entity.getBeneficiaryReference())
                .ownerReference(entity.getOwnerReference())
                .ownerType(entity.getOwnerType())
                .name(entity.getName())
                .email(entity.getEmail())
                .contactNumber(entity.getContactNumber())
                .gatewayName(entity.getGatewayName())
                .gatewayContactId(entity.getGatewayContactId())
                .status(entity.getStatus())
                .metadata(entity.getMetadata())
                .build();
    }

    public FundAccountResponse toFundAccountResponse(FundAccountEntity entity){
        if (entity==null){
            return null;
        }
        return FundAccountResponse.builder()
                .fundAccountReference(entity.getFundAccountReference())
                .fundAccountType(entity.getFundAccountType())
                .accountHolderName(entity.getAccountHolderName())
                .accountNumber(entity.getAccountNumber())
                .ifscCode(entity.getIfscCode())
                .vpa(entity.getVpa())
                .isDefault(entity.getIsDefault())
                .status(entity.getStatus())
                .build();
    }

    public List<FundAccountResponse> toFundAccountResponse(List<FundAccountEntity> entities){
        if (entities==null || entities.isEmpty()){
            return Collections.emptyList();
        }
        return entities.stream().map(this::toFundAccountResponse).toList();
    }
    private String maskAccountNumber(String accountNumber){
        if (accountNumber==null || accountNumber.isBlank()){
            return null;
        }
        if (accountNumber.length()<=4){
            return accountNumber;
        }
        return "*".repeat(accountNumber.length()-4)
                +accountNumber.substring(accountNumber.length()-4);
    }

}
