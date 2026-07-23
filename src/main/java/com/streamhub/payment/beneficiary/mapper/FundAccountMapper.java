package com.streamhub.payment.beneficiary.mapper;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.dto.response.CreateFundAccountResponse;
import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.entity.FundAccountEntity;
import org.springframework.stereotype.Component;

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


}
