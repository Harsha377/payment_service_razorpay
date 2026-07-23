package com.streamhub.payment.beneficiary.service.impl;

import com.streamhub.payment.beneficiary.dto.request.CreateFundAccountRequest;
import com.streamhub.payment.beneficiary.dto.response.CreateFundAccountResponse;
import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.entity.FundAccountEntity;
import com.streamhub.payment.beneficiary.enums.BeneficiaryStatus;
import com.streamhub.payment.beneficiary.enums.FundAccountStatus;
import com.streamhub.payment.beneficiary.enums.FundAccountType;
import com.streamhub.payment.beneficiary.mapper.FundAccountMapper;
import com.streamhub.payment.beneficiary.mapper.GatewayFundAccountMapper;
import com.streamhub.payment.beneficiary.repository.BeneficiaryRepository;
import com.streamhub.payment.beneficiary.repository.FundAccountRepository;
import com.streamhub.payment.beneficiary.service.FundAccountService;
import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ConflictException;
import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.gateway.dto.request.GatewayContactRequest;
import com.streamhub.payment.gateway.dto.request.GatewayFundAccountRequest;
import com.streamhub.payment.gateway.dto.response.GatewayContactResponse;
import com.streamhub.payment.gateway.dto.response.GatewayContactResult;
import com.streamhub.payment.gateway.dto.response.GatewayFundAccountResponse;
import com.streamhub.payment.gateway.dto.response.GatewayFundAccountResult;
import com.streamhub.payment.gateway.mapper.GatewayContactMapper;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.payment.enums.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FundAccountServiceImpl implements FundAccountService {
    private final BeneficiaryRepository beneficiaryRepository;
    private final FundAccountRepository fundAccountRepository;
    private final GatewayFundAccountMapper gatewayFundAccountMapper;
    private final GatewayContactMapper gatewayContactMapper;
    private final FundAccountMapper fundAccountMapper;
    private final RazorpayService razorpayService;

    @Override
    public CreateFundAccountResponse createFundAccount(CreateFundAccountRequest request) {
        log.info("Creating {} fund account for owner reference: {}",
                request.fundAccountType(), request.ownerReference());
        validateRequest(request);
        Optional<BeneficiaryEntity> existingBeneficiary =findBeneficiary(request);
        BeneficiaryEntity beneficiary;
        if (existingBeneficiary.isPresent()){
            beneficiary=existingBeneficiary.get();
            validateOwnerDetails(beneficiary,request);
            validateDuplicateFundAccount(beneficiary,request);
            log.info("Existing beneficiary found. Beneficiary Reference: {}",
                    beneficiary.getBeneficiaryReference());
        }
        else {
            beneficiary=createBeneficiary(request);
            GatewayContactRequest gatewayRequest =
                    gatewayContactMapper.toGatewayContactRequest(request);
            GatewayContactResult gatewayResult  =
                    razorpayService.createContact(gatewayRequest);
            GatewayContactResponse gatewayResponse = gatewayResult.getResponse();
            log.info("Gateway contact created successfully. Gateway Contact Id: {}",
                    gatewayResponse.getId());
            beneficiary.setGatewayContactId(gatewayResponse.getId());
            beneficiary.setGatewayResponse(gatewayResult.getGatewayResponse());
            beneficiary=saveBeneficiary(beneficiary);
            log.info("Beneficiary created successfully. Beneficiary Reference: {}",
                    beneficiary.getBeneficiaryReference());

        }
        FundAccountEntity fundAccount = createFundAccount(request, beneficiary);

        GatewayFundAccountRequest gatewayFundAccountRequest =
                gatewayFundAccountMapper.toGatewayFundAccountRequest(request,beneficiary);

        GatewayFundAccountResult gatewayFundAccountResult =
                razorpayService.createFundAccount(gatewayFundAccountRequest);

        GatewayFundAccountResponse gatewayFundAccountResponse = gatewayFundAccountResult.getResponse();

        log.info("Gateway Fund Account created successfully. Gateway Fund Account Id: {}",
                gatewayFundAccountResponse.getId());

        fundAccount.setGatewayFundAccountId(gatewayFundAccountResponse.getId());
        fundAccount.setGatewayResponse(gatewayFundAccountResult.getGatewayResponse());

        fundAccount=saveFundAccount(fundAccount);
        log.info("Fund account created successfully. Fund Account Reference: {}",
                fundAccount.getFundAccountReference());
        return fundAccountMapper.toCreateFundAccountResponse(beneficiary,fundAccount);

    }

    private void validateRequest(CreateFundAccountRequest request){
        if (request.fundAccountType() == FundAccountType.BANK_ACCOUNT
                && request.vpa() != null && !request.vpa().isBlank()) {

            throw new BadRequestException("VPA must not be provided for BANK_ACCOUNT.");
        }
        if(request.fundAccountType()== FundAccountType.BANK_ACCOUNT){
            if (request.accountHolderName()==null || request.accountHolderName().isBlank()){
                throw new BadRequestException("Account holder name is required.");
            }

            if (request.accountNumber()==null || request.accountNumber().isBlank()){
                throw new BadRequestException("Account number is required.");
            }

            if (request.ifscCode()==null || request.ifscCode().isBlank()){
                throw new BadRequestException("IFSC code is required.");
            }

        } else if (request.fundAccountType()==FundAccountType.VPA) {
            if (request.accountHolderName() != null || request.accountNumber() != null
                    || request.ifscCode() != null) {

                throw new BadRequestException("Bank account details must not be provided for VPA.");
            }

            if (request.vpa()==null || request.vpa().isBlank()){
                throw new BadRequestException("VPA is required.");
            }
        }
    }

    private Optional<BeneficiaryEntity> findBeneficiary(CreateFundAccountRequest request){
        return beneficiaryRepository.findByOwnerReferenceAndOwnerType(request.ownerReference(), request.ownerType());
    }
    private BeneficiaryEntity createBeneficiary(CreateFundAccountRequest request){
        BeneficiaryEntity beneficiary=fundAccountMapper.toBeneficiaryEntity(request);
        beneficiary.setBeneficiaryReference(UlidGenerator.generate());
        beneficiary.setGatewayName(PaymentGateway.RAZORPAY);
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        return beneficiary;
    }

    private FundAccountEntity createFundAccount(CreateFundAccountRequest request,BeneficiaryEntity beneficiary){
        FundAccountEntity fundAccount = fundAccountMapper.toFundAccountEntity(request);
        fundAccount.setFundAccountReference(UlidGenerator.generate());
        fundAccount.setBeneficiary(beneficiary);
        fundAccount.setStatus(FundAccountStatus.ACTIVE);
        // First fund account is default
        boolean hasDefault =
                fundAccountRepository.existsByBeneficiaryAndIsDefaultTrue(
                        beneficiary);

        fundAccount.setIsDefault(!hasDefault);
        return fundAccount;
    }

    private  BeneficiaryEntity saveBeneficiary(BeneficiaryEntity beneficiary){
        return beneficiaryRepository.save(beneficiary);
    }

    private FundAccountEntity saveFundAccount(FundAccountEntity fundAccount){
        return fundAccountRepository.save(fundAccount);
    }

    private void validateOwnerDetails(BeneficiaryEntity beneficiary,CreateFundAccountRequest request){
        if (!beneficiary.getName().equals(request.name())){
            throw new ConflictException("Owner name does not match the existing beneficiary.");
        }
        if (!beneficiary.getEmail().equalsIgnoreCase(request.email())) {
            throw new ConflictException(
                    "Owner email does not match the existing beneficiary.");
        }

        if (!beneficiary.getContactNumber().equals(request.contactNumber())) {
            throw new ConflictException(
                    "Owner contact number does not match the existing beneficiary.");
        }
    }

    private void validateDuplicateFundAccount(BeneficiaryEntity beneficiary,CreateFundAccountRequest request){
        fundAccountRepository.findByBeneficiaryAndFundAccountType(beneficiary,request.fundAccountType())
                .ifPresent(fundAccount -> {
                    throw new ConflictException(request.fundAccountType()
                            + " already exists for this beneficiary.");
                });

    }

}
