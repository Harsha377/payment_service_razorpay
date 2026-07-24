package com.streamhub.payment.beneficiary.controller;

import com.streamhub.payment.beneficiary.dto.response.BeneficiaryPageResponse;
import com.streamhub.payment.beneficiary.dto.response.BeneficiaryResponse;
import com.streamhub.payment.beneficiary.dto.response.FundAccountResponse;
import com.streamhub.payment.beneficiary.service.FundAccountService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {
    private final FundAccountService fundAccountService;

    @GetMapping("/{beneficiaryReference}")
    public ResponseEntity<BeneficiaryResponse> getBeneficiary(@PathVariable @NotNull(message = "beneficiary reference cannot be null") String beneficiaryReference){
        BeneficiaryResponse response = fundAccountService.getBeneficiary(beneficiaryReference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{beneficiaryReference}/fund-accounts")
    public ResponseEntity<List<FundAccountResponse>> getFundAccounts(
            @PathVariable @NotNull(message = "Beneficiary reference is required.") String beneficiaryReference){
        List<FundAccountResponse> response =
                fundAccountService.getFundAccounts(beneficiaryReference);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<BeneficiaryPageResponse>  getBeneficiaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        BeneficiaryPageResponse response = fundAccountService.getBeneficiaries(page, size);
        return ResponseEntity.ok(response);
    }

}
