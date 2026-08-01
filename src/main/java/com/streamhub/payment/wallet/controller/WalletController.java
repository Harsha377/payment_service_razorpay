package com.streamhub.payment.wallet.controller;

import com.streamhub.payment.wallet.dto.WalletResponse;
import com.streamhub.payment.wallet.dto.request.CreateWalletRequest;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.enums.WalletOwnerType;
import com.streamhub.payment.wallet.mapper.WalletMapper;
import com.streamhub.payment.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test/admin surface for the owner-based wallet model.
 * <p>
 * In the real flow wallets are created implicitly by
 * {@link WalletService#getOrCreateWallet} when a settlement or refund
 * first credits an owner - this controller exists so a wallet's
 * existence and balance can be created/inspected directly while
 * building and testing that flow, without needing a real payment first.
 */
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;
    private final WalletMapper walletMapper;

    /**
     * Explicitly creates a wallet for an owner. Fails with 409 if a wallet
     * for this (ownerReference, ownerType) already exists - use
     * GET /api/v1/wallets/{ownerType}/{ownerReference} to fetch it instead.
     */
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        WalletEntity wallet = walletService.createWallet(request.ownerReference(), request.ownerType());
        return ResponseEntity.status(HttpStatus.CREATED).body(walletMapper.toWalletResponse(wallet));
    }

    /**
     * Fetches a wallet by its owner. 404 if no wallet has been created for
     * this owner yet (e.g. no settlement/refund has touched it, and nobody
     * has explicitly created one via POST).
     */
    @GetMapping("/{ownerType}/{ownerReference}")
    public ResponseEntity<WalletResponse> getWallet(
            @PathVariable WalletOwnerType ownerType,
            @PathVariable String ownerReference) {

        WalletEntity wallet = walletService.findWalletByOwner(ownerReference, ownerType);
        return ResponseEntity.ok(walletMapper.toWalletResponse(wallet));
    }
}
