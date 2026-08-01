package com.streamhub.payment.wallet.dto.request;

import com.streamhub.payment.wallet.enums.WalletOwnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request payload for POST /api/v1/wallets.
 * Mainly a testing/admin entry point - in the normal flow, wallets get
 * created implicitly (see WalletService#getOrCreateWallet) the first
 * time a settlement or refund credits an owner who doesn't have one yet.
 */
@Builder
public record CreateWalletRequest(

        @NotBlank(message = "Owner reference is required.")
        @Size(max = 26, message = "Owner reference must not exceed 26 characters.")
        String ownerReference,

        @NotNull(message = "Owner type is required.")
        WalletOwnerType ownerType

) {
}
