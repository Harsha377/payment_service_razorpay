package com.streamhub.payment.wallet.repository;

import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity,Long> {
    Optional<WalletTransactionEntity> findByWalletTransactionReference(String walletTransactionReference);
    List<WalletTransactionEntity> findByWalletOrderByCreatedAtDesc(WalletEntity wallet);
}
