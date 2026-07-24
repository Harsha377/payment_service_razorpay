package com.streamhub.payment.beneficiary.controller;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.dto.response.*;
import com.streamhub.payment.beneficiary.service.FundAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PatchMapping("/{fundAccountReference}/default")
    public ResponseEntity<SetDefaultFundAccountResponse> setDefaultFundAccount(@PathVariable String fundAccountReference){
        SetDefaultFundAccountResponse response=fundAccountService.setDefaultFundAccount(fundAccountReference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fundAccountReference}")
    public ResponseEntity<FundAccountResponse> getFundAccount(
            @PathVariable String fundAccountReference) {

        FundAccountResponse response =
                fundAccountService.getFundAccount(fundAccountReference);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fundAccountReference}")
    public ResponseEntity<DeleteFundAccountResponse> deleteFundAccount(
            @PathVariable String fundAccountReference) {

        DeleteFundAccountResponse response =
                fundAccountService.deleteFundAccount(fundAccountReference);

        return ResponseEntity.ok(response);
    }


}
