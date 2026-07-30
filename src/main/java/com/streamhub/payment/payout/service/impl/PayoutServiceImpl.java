package com.streamhub.payment.payout.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.entity.FundAccountEntity;
import com.streamhub.payment.beneficiary.enums.BeneficiaryStatus;
import com.streamhub.payment.beneficiary.enums.FundAccountStatus;
import com.streamhub.payment.beneficiary.repository.BeneficiaryRepository;
import com.streamhub.payment.beneficiary.repository.FundAccountRepository;
import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ConflictException;
import com.streamhub.payment.common.exception.ResourceNotFoundException;
import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.gateway.dto.request.GatewayPayoutRequest;
import com.streamhub.payment.gateway.dto.response.GatewayPayoutDetailsResponse;
import com.streamhub.payment.gateway.dto.response.GatewayPayoutResponse;
import com.streamhub.payment.gateway.dto.response.GatewayPayoutResult;
import com.streamhub.payment.gateway.mapper.GatewayPayoutMapper;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.payment.enums.PaymentGateway;
import com.streamhub.payment.payout.dto.request.CreatePayoutRequest;
import com.streamhub.payment.payout.dto.response.CreatePayoutResponse;
import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.payout.entity.PayoutEventEntity;
import com.streamhub.payment.payout.enums.PayoutEventSource;
import com.streamhub.payment.payout.enums.PayoutEventType;
import com.streamhub.payment.payout.enums.PayoutStatus;
import com.streamhub.payment.payout.mapper.PayoutMapper;
import com.streamhub.payment.payout.repository.PayoutEventRepository;
import com.streamhub.payment.payout.repository.PayoutRepository;
import com.streamhub.payment.payout.service.PayoutService;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.entity.WalletHoldEntity;
import com.streamhub.payment.wallet.enums.WalletHoldStatus;
import com.streamhub.payment.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Locale;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PayoutServiceImpl implements PayoutService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final FundAccountRepository fundAccountRepository;
    private final PayoutRepository payoutRepository;
    private final PayoutEventRepository payoutEventRepository;
    private final PayoutMapper payoutMapper;
    private final GatewayPayoutMapper gatewayPayoutMapper;
    private final RazorpayService razorpayService;
    private final WalletService walletService;

    @Override
    public CreatePayoutResponse createPayout(CreatePayoutRequest request, String idempotencyKey) {
        log.info("Creating payout. Beneficiary Reference: {}, Amount: {}, Currency: {}, Idempotency Key: {}",
                request.getBeneficiaryReference(), request.getAmount(),
                request.getCurrency(), idempotencyKey);

        validateRequest(request);
        validateIdempotency(idempotencyKey);

        BeneficiaryEntity beneficiary = findBeneficiary(request);
        FundAccountEntity fundAccount = findDefaultFundAccount(beneficiary);
        WalletEntity wallet = findWallet(beneficiary);
        WalletHoldEntity walletHold =
                walletService.createWalletHold(wallet, request.getAmount());

        PayoutEntity payout = createPayoutEntity(request, idempotencyKey, beneficiary, fundAccount);
        payout = savePayout(payout);

        try {
            GatewayPayoutResult gatewayResult = createGatewayPayout(request, fundAccount, payout);
            log.info("Gateway payout created. Payout Reference: {}, Gateway Payout Id: {}, Status: {}",
                    payout.getPayoutReference(),
                    gatewayResult.getResponse().getId(), gatewayResult.getResponse().getStatus());

            payout = updatePayout(payout, gatewayResult);

            if (payout.getStatus() == PayoutStatus.FAILED
                    || payout.getStatus() == PayoutStatus.CANCELLED
                    || payout.getStatus() == PayoutStatus.REVERSED) {

                walletService.cancelWalletHoldBeforePayout(walletHold);

                throw new ConflictException(
                        "Gateway payout creation failed.");
            }

            walletHold = walletService.attachPayoutToWalletHold(walletHold, payout);
            walletService.createPayoutWalletTransaction(walletHold, payout);

            createPayoutEvent(payout, gatewayResult);
        }
        catch (Exception ex) {

            if (walletHold.getPayout() == null && walletHold.getStatus() == WalletHoldStatus.ACTIVE) {

                walletService.cancelWalletHoldBeforePayout(walletHold);
            }

            throw ex;
        }
        log.info("Payout created successfully. Payout Reference: {}, Status: {}",
                payout.getPayoutReference(), payout.getStatus());

        return payoutMapper.toCreatePayoutResponse(payout);
    }

    private void validateRequest(CreatePayoutRequest request) {
        if (!"INR".equalsIgnoreCase(request.getCurrency())) {
            throw new BadRequestException("Only INR currency is supported.");
        }
        if (request.getAmount() <= 0) {
            throw new BadRequestException("Amount must be greater than zero.");
        }
    }

    private void validateIdempotency(String idempotencyKey) {
        payoutRepository
                .findByIdempotencyKey(idempotencyKey)
                .ifPresent(payout -> {
                    log.warn("Duplicate payout request detected. Idempotency Key: {}",
                            idempotencyKey);
                    throw new ConflictException("Payout already exists for the provided idempotency key.");
                });

    }

    private BeneficiaryEntity findBeneficiary(CreatePayoutRequest request) {
        BeneficiaryEntity beneficiary = beneficiaryRepository.findByBeneficiaryReference(request.getBeneficiaryReference())
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        if (beneficiary.getStatus() != BeneficiaryStatus.ACTIVE) {
            log.warn("Beneficiary is inactive. Reference: {}",
                    beneficiary.getBeneficiaryReference());
            throw new ConflictException("Beneficiary is not active.");
        }
        log.info("Beneficiary found. Reference: {}", beneficiary.getBeneficiaryReference());
        return beneficiary;
    }

    private FundAccountEntity findDefaultFundAccount(BeneficiaryEntity beneficiary) {
        return fundAccountRepository.findByBeneficiaryAndIsDefaultTrueAndStatus(beneficiary, FundAccountStatus.ACTIVE)
                .orElseThrow(() ->{
                            log.warn("No active default fund account found for beneficiary. Reference: {}",
                                    beneficiary.getBeneficiaryReference());
                            return new ConflictException("No active default fund account found for the beneficiary.");
                }

                );
    }

    private PayoutEntity createPayoutEntity(
            CreatePayoutRequest request, String idempotencyKey,
            BeneficiaryEntity beneficiary, FundAccountEntity fundAccount) {
        PayoutEntity payout = payoutMapper.toPayoutEntity(request, beneficiary, fundAccount);
        payout.setPayoutReference(UlidGenerator.generate());
        payout.setIdempotencyKey(idempotencyKey);
        payout.setGatewayName(PaymentGateway.RAZORPAY);
        payout.setStatus(PayoutStatus.CREATED);
        log.info("Payout entity created. Payout Reference: {}", payout.getPayoutReference());

        return payout;

    }

    private PayoutEntity savePayout(PayoutEntity payout) {
        return payoutRepository.save(payout);
    }

    private GatewayPayoutResult createGatewayPayout(
            CreatePayoutRequest request,
            FundAccountEntity fundAccount,
            PayoutEntity payout
    ) {
        log.info("Initiating Razorpay payout. Payout Reference: {}", payout.getPayoutReference());
        GatewayPayoutRequest gatewayRequest =
                gatewayPayoutMapper.toGatewayPayoutRequest(request, fundAccount, payout);
        return razorpayService.createPayout(gatewayRequest);

    }

    private PayoutEntity updatePayout(PayoutEntity payout, GatewayPayoutResult gatewayPayoutResult) {
        GatewayPayoutResponse response = gatewayPayoutResult.getResponse();
        payout.setGatewayPayoutId(response.getId());
        payout.setGatewayResponse(gatewayPayoutResult.getGatewayResponse());
        payout.setStatus(mapGatewayStatus(response.getStatus()));
        return payoutRepository.save(payout);
    }

    private PayoutStatus mapGatewayStatus(String status) {
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "queued" -> PayoutStatus.CREATED;
            case "pending" -> PayoutStatus.PENDING;
            case "processing" -> PayoutStatus.PROCESSING;
            case "processed" -> PayoutStatus.PROCESSED;
            case "failed" -> PayoutStatus.FAILED;
            case "cancelled" -> PayoutStatus.CANCELLED;
            case "reversed" -> PayoutStatus.REVERSED;
            default -> throw new BadRequestException(
                    "Unsupported payout status received from Razorpay.");
        };
    }

    private void createPayoutEvent(PayoutEntity payout, GatewayPayoutResult gatewayPayoutResult) {
        PayoutEventEntity payoutEvent = PayoutEventEntity.builder()
                .payoutEventReference(UlidGenerator.generate())
                .payout(payout)
                .eventType(mapEventType(payout.getStatus()))
                .eventSource(PayoutEventSource.API)
                .eventData(gatewayPayoutResult.getGatewayResponse())
                .build();
        payoutEventRepository.save(payoutEvent);
        log.info("Payout event created. Event Type: {}, Payout Reference: {}",
                payoutEvent.getEventType(), payout.getPayoutReference());
    }

    private PayoutEventType mapEventType(PayoutStatus status) {
        return switch (status) {
            case CREATED -> PayoutEventType.PAYOUT_CREATED;
            case PENDING -> PayoutEventType.PAYOUT_PENDING;
            case PROCESSING -> PayoutEventType.PAYOUT_PROCESSING;
            case PROCESSED -> PayoutEventType.PAYOUT_PROCESSED;
            case FAILED -> PayoutEventType.PAYOUT_FAILED;
            case CANCELLED -> PayoutEventType.PAYOUT_CANCELLED;
            case REVERSED -> PayoutEventType.PAYOUT_REVERSED;
        };
    }

    @Override
    public GatewayPayoutDetailsResponse getGatewayPayout(
            String gatewayPayoutId) {

        log.info(
                "Fetching payout from Razorpay. Gateway Payout Id: {}",
                gatewayPayoutId);

        if (gatewayPayoutId == null || gatewayPayoutId.isBlank()) {
            throw new BadRequestException(
                    "Gateway payout id is required.");
        }

        GatewayPayoutDetailsResponse response =
                razorpayService.fetchPayout(gatewayPayoutId);

        log.info(
                "Successfully fetched payout from Razorpay. Gateway Payout Id: {}, Status: {}",
                response.getId(),
                response.getStatus());

        return response;
    }

    @Override
    @Transactional
    public void updatePayoutFromWebhook(PayoutEntity payout, PayoutStatus status, PayoutEventType eventType, JsonNode webhookPayload, JsonNode payoutNode) {
        log.info("Updating payout from webhook. Payout Reference: {}, Gateway Payout Id: {}, Current Status: {}, New Status: {}",
                payout.getPayoutReference(), payout.getGatewayPayoutId(),
                payout.getStatus(), status);

        // Update payout
        payout.setStatus(status);
        // Store complete webhook payload
        payout.setGatewayResponse(webhookPayload);

        // Optional fields if present in webhook
        // UTR
        if (payoutNode.hasNonNull("utr")) {
            payout.setUtr(payoutNode.get("utr").asText());
        }

        // Razorpay processing fee (in smallest currency unit)
        if (payoutNode.hasNonNull("fees")) {
            payout.setFees(payoutNode.get("fees").asLong());
        }

        // Tax charged on the fee
        if (payoutNode.hasNonNull("tax")) {
            payout.setTax(payoutNode.get("tax").asLong());
        }
        // Failure reason
        if (payoutNode.hasNonNull("failure_reason")) {
            payout.setFailureReason(
                    payoutNode.get("failure_reason").asText());
        }

        // Update timestamps based on payout status
        switch (status) {

            case PROCESSED -> payout.setProcessedAt(OffsetDateTime.now());

            case FAILED -> payout.setFailedAt(OffsetDateTime.now());

            case REVERSED -> payout.setReversedAt(OffsetDateTime.now());

            default -> {
                // CREATED, PENDING, PROCESSING
            }
        }

        payout=payoutRepository.save(payout);

        log.info("Payout updated successfully. Payout Reference: {}, Status: {}",
                payout.getPayoutReference(), payout.getStatus());

        // Create immutable event
        PayoutEventEntity payoutEvent = PayoutEventEntity.builder()
                .payoutEventReference(UlidGenerator.generate())
                .payout(payout)
                .eventType(eventType)
                .eventSource(PayoutEventSource.WEBHOOK)
                .eventData(webhookPayload)
                .build();

        payoutEventRepository.save(payoutEvent);

        log.info("Payout event created successfully. Payout Reference: {}, Event Type: {}",
                payout.getPayoutReference(), payoutEvent.getEventType());

        WalletHoldEntity walletHold =
                walletService.findWalletHoldByPayout(payout);

        switch (status) {

            case PROCESSED ->
                    walletService.releaseWalletHold(walletHold);

            case FAILED,
                 REVERSED,
                 CANCELLED ->
                    walletService.cancelWalletHold(walletHold);

            default -> {
                // CREATED, PENDING, PROCESSING
            }
        }

    }
    private WalletEntity findWallet(BeneficiaryEntity beneficiary) {

        return walletService.findWalletByBeneficiary(beneficiary);
    }


}
