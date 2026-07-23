package com.streamhub.payment.beneficiary.service;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.dto.response.CreateFundAccountResponse;

public interface FundAccountService {
    CreateFundAccountResponse createFundAccount(CreateFundAccountRequest request);
}
