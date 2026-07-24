package com.streamhub.payment.beneficiary.service;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.dto.response.*;
import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.entity.FundAccountEntity;

import java.util.List;

public interface FundAccountService {
    CreateFundAccountResponse createFundAccount(CreateFundAccountRequest request);
    BeneficiaryResponse getBeneficiary(String beneficiaryReference);
    List<FundAccountResponse> getFundAccounts(String beneficiaryReference);
    SetDefaultFundAccountResponse setDefaultFundAccount(String fundAccountReference);
    FundAccountResponse getFundAccount(String fundAccountReference);
    DeleteFundAccountResponse deleteFundAccount(String fundAccountReference);
    BeneficiaryPageResponse getBeneficiaries(int page,int size);
    List<FundAccountEntity> findByBeneficiaryOrderByCreatedAtDesc(BeneficiaryEntity beneficiary);
}
