package com.streamhub.payment.wallet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.constants.CurrencyConstants;
import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ConflictException;
import com.streamhub.payment.common.exception.ResourceNotFoundException;
import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.payout.enums.PayoutStatus;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.entity.WalletHoldEntity;
import com.streamhub.payment.wallet.entity.WalletTransactionEntity;
import com.streamhub.payment.wallet.enums.*;
import com.streamhub.payment.wallet.repository.WalletHoldRepository;
import com.streamhub.payment.wallet.repository.WalletRepository;
import com.streamhub.payment.wallet.repository.WalletTransactionRepository;
import com.streamhub.payment.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletHoldRepository walletHoldRepository;

    @Override
    public WalletEntity createWallet(String ownerReference, WalletOwnerType ownerType) {
        log.info("Creating wallet. Owner Reference: {}, Owner Type: {}", ownerReference, ownerType);
        validateOwner(ownerReference, ownerType);
        validateWalletNotExists(ownerReference, ownerType);
        WalletEntity wallet = buildWallet(ownerReference, ownerType);
        wallet = saveWallet(wallet);
        log.info("Wallet created successfully. Wallet Reference: {}, Owner Reference: {}, Owner Type: {}",
                wallet.getWalletReference(),
                ownerReference,
                ownerType);
        return wallet;
    }

    @Override
    public WalletEntity getOrCreateWallet(String ownerReference, WalletOwnerType ownerType) {
        validateOwner(ownerReference, ownerType);
        return walletRepository.findByOwnerReferenceAndOwnerType(ownerReference, ownerType)
                .orElseGet(() -> createWallet(ownerReference, ownerType));
    }

    private void validateOwner(String ownerReference, WalletOwnerType ownerType){
        if (ownerReference == null || ownerReference.isBlank()){
            throw new BadRequestException("Wallet owner reference is required.");
        }
        if (ownerType == null){
            throw new BadRequestException("Wallet owner type is required.");
        }
    }
    private void validateWalletNotExists(String ownerReference, WalletOwnerType ownerType){
        if (walletRepository.existsByOwnerReferenceAndOwnerType(ownerReference, ownerType)){
            throw new ConflictException("Wallet already exists for owner.");
        }
    }

    private WalletEntity buildWallet(String ownerReference, WalletOwnerType ownerType){
        return WalletEntity.builder()
                .walletReference(UlidGenerator.generate())
                .ownerReference(ownerReference)
                .ownerType(ownerType)
                .availableBalance(0L)
                .pendingBalance(0L)
                .totalCredited(0L)
                .totalDebited(0L)
                .currency(CurrencyConstants.INR)
                .status(WalletStatus.ACTIVE)
                .build();

    }
    private WalletEntity saveWallet(WalletEntity wallet){
        log.info("Persisting wallet. Wallet Reference: {}",wallet.getWalletReference());
        return walletRepository.save(wallet);
    }

    @Override
    public void creditWallet(WalletEntity wallet, Long amount, WalletTransactionSource transactionSource, WalletReferenceType referenceType, String referenceId, String description, JsonNode metadata) {

        log.info("Crediting wallet. Wallet Reference: {}, Amount: {}",
                wallet.getWalletReference(), amount);
        validateWallet(wallet);
        validateAmount(amount);
        Long balanceBefore = wallet.getAvailableBalance();
        updateWallet(wallet,amount);
        wallet = saveWallet(wallet);
        createWalletTransaction(
                wallet,
                WalletTransactionType.CREDIT,
                transactionSource,
                amount,
                balanceBefore,
                wallet.getAvailableBalance(),
                referenceType,
                referenceId,
                description,
                metadata );
        log.info("Wallet credited successfully. Wallet Reference: {}, Available Balance: {}",
                wallet.getWalletReference(),
                wallet.getAvailableBalance());

    }

    private void validateWallet(WalletEntity wallet){
        if (wallet==null){
            throw new ResourceNotFoundException("Wallet not found.");
        }

        if (wallet.getStatus() != WalletStatus.ACTIVE){
            throw new ConflictException("Wallet is not active.");
        }
    }

    private void validateAmount(Long amount){
        if (amount==null || amount<=0){
            throw new ConflictException("Amount must be greater than zero.");
        }
    }

    private void updateWallet(WalletEntity wallet, Long amount) {
        wallet.setAvailableBalance(wallet.getAvailableBalance() + amount);
        wallet.setTotalCredited(wallet.getTotalCredited() + amount);
    }

    private void createWalletTransaction(
            WalletEntity wallet, WalletTransactionType transactionType,
            WalletTransactionSource transactionSource, Long amount, Long balanceBefore,
            Long balanceAfter, WalletReferenceType referenceType,
            String referenceId, String description, JsonNode metadata) {


        WalletTransactionEntity transaction = WalletTransactionEntity.builder()
                .walletTransactionReference(UlidGenerator.generate())
                .wallet(wallet)
                .transactionType(transactionType)
                .transactionSource(transactionSource)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .description(description)
                .metadata(metadata)
                .build();
        walletTransactionRepository.save(transaction);
        log.info("Wallet transaction created. Transaction Reference: {}, Wallet Reference: {}",
                transaction.getWalletTransactionReference(), wallet.getWalletReference());
    }

    @Override
    public WalletEntity  debitWallet(WalletEntity wallet, Long amount,
                                     WalletTransactionSource transactionSource,
                                     WalletReferenceType referenceType,
                                     String referenceId, String description,
                                     JsonNode metadata) {
        log.info("Debiting wallet. Wallet Reference: {}, amount: {}",wallet.getWalletReference(),amount);
        validateWallet(wallet);
        validateAmount(amount);
        validateAvailableBalance(wallet,amount);
        Long balanceBefore =wallet.getAvailableBalance();
        updateWalletForDebit(wallet,amount);
        wallet=saveWallet(wallet);
        createWalletTransaction(
                wallet,
                WalletTransactionType.DEBIT,
                transactionSource,
                amount,
                balanceBefore,
                wallet.getAvailableBalance(),
                referenceType,
                referenceId,
                description,
                metadata );

        log.info("Wallet debited successfully. Wallet Reference: {}, Available Balance: {}",
                wallet.getWalletReference(),
                wallet.getAvailableBalance());

        return wallet;


    }

    private void validateAvailableBalance(WalletEntity wallet,Long amount){
        if (wallet.getAvailableBalance()<amount){
            throw  new ConflictException("Insufficient wallet balance.");
        }
    }

    private void updateWalletForDebit(WalletEntity wallet,Long amount){
        wallet.setAvailableBalance(wallet.getAvailableBalance()- amount);
        wallet.setTotalDebited(wallet.getTotalDebited()+amount);
    }

    //    -----withdraw---
    @Override
    @Transactional
    public WalletHoldEntity createWalletHold(WalletEntity wallet, Long amount) {
        log.info("Creating wallet hold. Wallet Reference: {}, Amount: {}",
                wallet.getWalletReference(), amount);
        validateWallet(wallet);
        validateAmount(amount);
        validateAvailableBalance(wallet,amount);

        Long balanceBefore = wallet.getAvailableBalance();

        reserveWalletBalance(wallet,amount);
        Long balanceAfter = wallet.getAvailableBalance();

        wallet=saveWallet(wallet);

        WalletHoldEntity walletHold = buildWalletHold(wallet, amount,
                balanceBefore, balanceAfter);
        walletHold=saveWalletHold(walletHold);

        log.info("Wallet hold created successfully. Hold Reference: {}, Wallet Reference: {}",
                walletHold.getWalletHoldReference(), wallet.getWalletReference());

        return walletHold;
    }

    private void reserveWalletBalance(WalletEntity wallet,Long amount){
        log.info("Reserving wallet balance. Wallet Reference: {}, Amount: {}",
                wallet.getWalletReference(), amount);
        wallet.setAvailableBalance(wallet.getAvailableBalance()-amount);
        wallet.setPendingBalance(wallet.getPendingBalance()+amount);
    }

    private WalletHoldEntity buildWalletHold(
            WalletEntity wallet,
            Long amount,
            Long balanceBefore,
            Long balanceAfter) {

        return WalletHoldEntity.builder()
                .walletHoldReference(UlidGenerator.generate())
                .wallet(wallet)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(WalletHoldStatus.ACTIVE)
                .expiresAt(OffsetDateTime.now().plusMinutes(30))
                .build();
    }

    private WalletHoldEntity saveWalletHold(WalletHoldEntity walletHold){
        log.info("Persisting wallet hold. Hold Reference: {}", walletHold.getWalletHoldReference());

        return walletHoldRepository.save(walletHold);
    }

    @Override
    @Transactional
    public void releaseWalletHold(WalletHoldEntity walletHold) {
        log.info("Releasing wallet hold. Hold Reference: {}",
                walletHold.getWalletHoldReference());
        validateWalletHold(walletHold);
        validateWalletHoldStatus(walletHold);
        validatePayoutAttached(walletHold);
        validateWalletHoldNotExpired(walletHold);

        WalletEntity wallet =walletHold.getWallet();
        validateWallet(wallet);

        updateWalletAfterRelease(wallet, walletHold.getAmount());

        wallet=saveWallet(wallet);
        updateWalletHold(walletHold);
        walletHold=saveWalletHold(walletHold);

        log.info("Wallet hold released successfully. Hold Reference: {}, Wallet Reference: {}",
                walletHold.getWalletHoldReference(),
                wallet.getWalletReference());

    }
    private void validateWalletHold(WalletHoldEntity walletHold){
        if(walletHold==null){
            throw new ResourceNotFoundException("Wallet hold not found.");
        }
    }
    private void validateWalletHoldStatus(WalletHoldEntity walletHold){
        if (walletHold.getStatus() != WalletHoldStatus.ACTIVE){
            throw new ConflictException("Wallet hold is not active.");
        }
    }
    private void validatePayoutAttached(WalletHoldEntity walletHold) {

        if (walletHold.getPayout() == null) {
            throw new ConflictException("Wallet hold is not linked to a payout.");
        }
    }
    private void updateWalletAfterRelease(WalletEntity wallet , Long amount){
        log.info("Finalizing wallet after payout. Wallet Reference: {}",
                wallet.getWalletReference());
        wallet.setPendingBalance(wallet.getPendingBalance()-amount);
        wallet.setTotalDebited(wallet.getTotalDebited()+amount);
    }

    private void updateWalletHold(WalletHoldEntity walletHold){
        walletHold.setStatus(WalletHoldStatus.RELEASED);
    }

    @Override
    @Transactional
    public WalletHoldEntity attachPayoutToWalletHold(
            WalletHoldEntity walletHold,
            PayoutEntity payout) {

        validateWalletHold(walletHold);

        if (walletHold.getPayout() != null) {
            throw new ConflictException("Wallet hold is already linked to a payout.");
        }
        if (payout == null) {
            throw new ResourceNotFoundException("Payout not found.");
        }
        if (payout.getStatus() != PayoutStatus.CREATED
                && payout.getStatus() != PayoutStatus.PENDING
                && payout.getStatus() != PayoutStatus.PROCESSING) {

            throw new ConflictException(
                    "Payout cannot be attached in its current status.");
        }

        walletHold.setPayout(payout);

        walletHold = saveWalletHold(walletHold);

        log.info(
                "Payout attached to wallet hold. Hold Reference: {}, Payout Reference: {}",
                walletHold.getWalletHoldReference(),
                payout.getPayoutReference());

        return walletHold;
    }

    @Override
    @Transactional
    public void createPayoutWalletTransaction(
            WalletHoldEntity walletHold,
            PayoutEntity payout) {

        validateWalletHold(walletHold);


        if (payout == null) {
            throw new ResourceNotFoundException("Payout not found.");
        }

        WalletEntity wallet = walletHold.getWallet();
        validateWallet(wallet);

        createWalletTransaction(
                wallet,
                WalletTransactionType.DEBIT,
                WalletTransactionSource.PAYOUT,
                payout.getAmount(),
                walletHold.getBalanceBefore(),
                walletHold.getBalanceAfter(),
                WalletReferenceType.PAYOUT,
                payout.getPayoutReference(),
                "Wallet amount reserved for payout.",
                null);

        log.info(
                "Wallet payout transaction created. Wallet Reference: {}, Payout Reference: {}",
                wallet.getWalletReference(),
                payout.getPayoutReference());
    }

    @Override
    @Transactional
    public void cancelWalletHold(WalletHoldEntity walletHold) {

        log.info("Cancelling wallet hold. Hold Reference: {}", walletHold.getWalletHoldReference());

        validateWalletHold(walletHold);
        validateWalletHoldStatus(walletHold);
        validateWalletHoldNotExpired(walletHold);
        validatePayoutAttached(walletHold);


        WalletEntity wallet = walletHold.getWallet();
        validateWallet(wallet);

        restoreWalletBalance(wallet, walletHold.getAmount());

        wallet = saveWallet(wallet);

        updateWalletHoldToCancelled(walletHold);
        walletHold = saveWalletHold(walletHold);

        log.info("Wallet hold cancelled successfully. Hold Reference: {}, Wallet Reference: {}",
                walletHold.getWalletHoldReference(), wallet.getWalletReference());

    }

    private void restoreWalletBalance(WalletEntity wallet, Long amount) {

        log.info("Restoring wallet balance. Wallet Reference: {}, Amount: {}",
                wallet.getWalletReference(), amount);

        wallet.setAvailableBalance(wallet.getAvailableBalance() + amount);
        wallet.setPendingBalance(wallet.getPendingBalance() - amount);
    }

    private void updateWalletHoldToCancelled(WalletHoldEntity walletHold) {

        walletHold.setStatus(WalletHoldStatus.CANCELLED);
    }

    private void validateWalletHoldNotExpired(
            WalletHoldEntity walletHold) {

        if (walletHold.getExpiresAt() != null &&
                walletHold.getExpiresAt().isBefore(OffsetDateTime.now())) {

            throw new ConflictException(
                    "Wallet hold has expired.");
        }
    }
    @Override
    public WalletEntity findWalletByOwner(String ownerReference, WalletOwnerType ownerType) {
        return walletRepository.findByOwnerReferenceAndOwnerType(ownerReference, ownerType)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wallet not found for owner reference: " + ownerReference));
    }

    @Override
    @Transactional
    public void cancelWalletHoldBeforePayout(
            WalletHoldEntity walletHold) {

        log.info(
                "Cancelling wallet hold before payout creation. Hold Reference: {}",
                walletHold.getWalletHoldReference());

        validateWalletHold(walletHold);
        validateWalletHoldStatus(walletHold);
        validateWalletHoldNotExpired(walletHold);

        WalletEntity wallet = walletHold.getWallet();
        validateWallet(wallet);

        restoreWalletBalance(wallet, walletHold.getAmount());

        wallet = saveWallet(wallet);

        walletHold.setStatus(WalletHoldStatus.CANCELLED);
        walletHold = saveWalletHold(walletHold);

        log.info(
                "Wallet hold cancelled successfully before payout creation. Hold Reference: {}, Wallet Reference: {}",
                walletHold.getWalletHoldReference(),
                wallet.getWalletReference());

    }

    @Override
    @Transactional(readOnly = true)
    public WalletHoldEntity findWalletHoldByPayout(PayoutEntity payout) {

        if (payout == null) {
            throw new ResourceNotFoundException("Payout not found.");
        }

        return walletHoldRepository.findByPayout(payout)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Wallet hold not found."));
    }

}
