package com.streamhub.payment.settlement.service.impl;

import com.github.f4b6a3.ulid.UlidCreator;
import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ConflictException;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.enums.PaymentStatus;
import com.streamhub.payment.settlement.dto.response.SettlementCalculation;
import com.streamhub.payment.settlement.entity.SettlementEntity;
import com.streamhub.payment.settlement.entity.SettlementEventEntity;
import com.streamhub.payment.settlement.enums.SettlementEventSource;
import com.streamhub.payment.settlement.enums.SettlementEventType;
import com.streamhub.payment.settlement.enums.SettlementStatus;
import com.streamhub.payment.settlement.mapper.SettlementMapper;
import com.streamhub.payment.settlement.repository.SettlementEventRepository;
import com.streamhub.payment.settlement.repository.SettlementRepository;
import com.streamhub.payment.settlement.service.SettlementService;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.enums.WalletOwnerType;
import com.streamhub.payment.wallet.enums.WalletReferenceType;
import com.streamhub.payment.wallet.enums.WalletTransactionSource;
import com.streamhub.payment.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Settlements move money from a captured payment into a wallet - either by a
 * flat platform-commission percentage, or by an explicit fixed split (e.g.
 * ₹1000 to a provider's wallet + ₹200 platform commission).
 * <p>
 * Neither path requires a BeneficiaryEntity to exist. The wallet is looked up
 * (or created on first payin, see {@link WalletService#getOrCreateWallet})
 * purely by (ownerReference, ownerType). A Beneficiary/fund-account is only
 * ever needed later, when an actual bank payout is triggered from the wallet.
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final SettlementEventRepository settlementEventRepository;
    private final WalletService walletService;
    private final SettlementMapper settlementMapper;

    private static final int PLATFORM_COMMISSION_PERCENT = 20;

    @Override
    public void createSettlement(PaymentEntity payment, String ownerReference) {
        validatePayment(payment);
        validateSettlementNotExists(payment);

        SettlementCalculation calculation = calculatePercentageSettlement(payment);

        processSettlement(payment, ownerReference, calculation);
    }

    @Override
    public void createSettlement(
            PaymentEntity payment, String ownerReference, Long netAmount, Long commissionAmount) {

        validatePayment(payment);
        validateSettlementNotExists(payment);
        validateSplitAmounts(payment, netAmount, commissionAmount);

        SettlementCalculation calculation =
                new SettlementCalculation(payment.getAmount(), commissionAmount, netAmount);

        processSettlement(payment, ownerReference, calculation);
    }

    private void processSettlement(
            PaymentEntity payment, String ownerReference, SettlementCalculation calculation) {

        validateOwnerReference(ownerReference);

        // Wallet is created on the fly if this is the owner's first payin -
        // no beneficiary / payout onboarding required at this point.
        WalletEntity wallet = walletService.getOrCreateWallet(ownerReference, WalletOwnerType.PROVIDER);

        SettlementEntity settlement =
                createSettlementEntity(payment, ownerReference, wallet, calculation);

        settlement = settlementRepository.save(settlement);

        walletService.creditWallet(
                wallet,
                calculation.getNetAmount(),
                WalletTransactionSource.SETTLEMENT,
                WalletReferenceType.SETTLEMENT,
                settlement.getSettlementReference(),
                "Settlement Credit",
                settlement.getMetadata()
        );

        createSettlementEvent(settlement);
        settlement.setStatus(SettlementStatus.COMPLETED);
        settlement = settlementRepository.save(settlement);

        settlementMapper.toSettlementResponse(settlement);
    }

    private void validatePayment(PaymentEntity payment) {

        if (payment == null) {
            throw new BadRequestException("Payment cannot be null.");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new ConflictException(
                    "Settlement can only be created for successful payments.");
        }

        if (!"captured".equalsIgnoreCase(payment.getGatewayStatus())) {
            throw new ConflictException(
                    "Gateway payment has not been captured.");
        }
    }

    private void validateSettlementNotExists(PaymentEntity payment) {

        if (settlementRepository.existsByPayment(payment)) {
            throw new ConflictException("Settlement already exists for payment reference: "
                    + payment.getPaymentReference());
        }
    }

    private void validateOwnerReference(String ownerReference) {
        if (ownerReference == null || ownerReference.isBlank()) {
            throw new BadRequestException("Settlement owner reference is required.");
        }
    }

    private void validateSplitAmounts(PaymentEntity payment, Long netAmount, Long commissionAmount) {

        if (netAmount == null || netAmount <= 0) {
            throw new BadRequestException("Net amount must be greater than zero.");
        }

        if (commissionAmount == null || commissionAmount < 0) {
            throw new BadRequestException("Commission amount cannot be negative.");
        }

        if (netAmount + commissionAmount != payment.getAmount()) {
            throw new BadRequestException(
                    "Net amount plus commission amount must equal the payment amount.");
        }
    }

    private SettlementCalculation calculatePercentageSettlement(PaymentEntity payment) {

        Long grossAmount = payment.getAmount();
        Long commissionAmount = (grossAmount * PLATFORM_COMMISSION_PERCENT) / 100;
        Long netAmount = grossAmount - commissionAmount;

        return new SettlementCalculation(grossAmount, commissionAmount, netAmount);
    }

    private SettlementEntity createSettlementEntity(
            PaymentEntity payment,
            String ownerReference,
            WalletEntity wallet,
            SettlementCalculation calculation) {

        SettlementEntity settlement = new SettlementEntity();

        settlement.setSettlementReference(UlidCreator.getUlid().toString());

        settlement.setPayment(payment);
        settlement.setOwnerReference(ownerReference);
        settlement.setOwnerType(WalletOwnerType.PROVIDER);
        settlement.setWallet(wallet);
        settlement.setGrossAmount(calculation.getGrossAmount());
        settlement.setCommissionAmount(calculation.getCommissionAmount());
        settlement.setNetAmount(calculation.getNetAmount());
        settlement.setCurrency(payment.getCurrency());
        settlement.setStatus(SettlementStatus.CREATED);
        settlement.setMetadata(null);

        return settlement;
    }

    private void createSettlementEvent(SettlementEntity settlement) {

        SettlementEventEntity event = new SettlementEventEntity();

        event.setSettlementEventReference(UlidCreator.getUlid().toString());
        event.setSettlement(settlement);
        event.setEventType(SettlementEventType.SETTLEMENT_COMPLETED);
        event.setEventSource(SettlementEventSource.SYSTEM);
        event.setEventData(settlement.getMetadata());
        settlementEventRepository.save(event);
    }
}
