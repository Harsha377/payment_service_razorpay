package com.streamhub.payment.beneficiary.controller;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.dto.response.CreateFundAccountResponse;
import com.streamhub.payment.beneficiary.service.FundAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fund-accounts")
public class FundAccountController {
    private final FundAccountService fundAccountService;

    @PostMapping
    public ResponseEntity<CreateFundAccountResponse> createFundAccount(
            @Valid @RequestBody CreateFundAccountRequest request) {

        CreateFundAccountResponse response =
                fundAccountService.createFundAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
