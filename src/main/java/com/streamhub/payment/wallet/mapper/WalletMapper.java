package com.streamhub.payment.wallet.mapper;


import com.streamhub.payment.wallet.dto.WalletResponse;
import com.streamhub.payment.wallet.entity.WalletEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResponse toWalletResponse(WalletEntity wallet) {
        return WalletResponse.builder()
                .walletReference(wallet.getWalletReference())
                .ownerReference(wallet.getOwnerReference())
                .ownerType(wallet.getOwnerType())
                .availableBalance(wallet.getAvailableBalance())
                .pendingBalance(wallet.getPendingBalance())
                .totalCredited(wallet.getTotalCredited())
                .totalDebited(wallet.getTotalDebited())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus())
                .build();
    }
}
