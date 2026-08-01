package com.streamhub.payment.wallet.repository;

import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.enums.WalletOwnerType;
import com.streamhub.payment.wallet.enums.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    Optional<WalletEntity> findByWalletReference(String walletReference);

    Optional<WalletEntity> findByOwnerReferenceAndOwnerType(String ownerReference, WalletOwnerType ownerType);

    Optional<WalletEntity> findByOwnerReferenceAndOwnerTypeAndStatus(
            String ownerReference, WalletOwnerType ownerType, WalletStatus status);

    boolean existsByOwnerReferenceAndOwnerType(String ownerReference, WalletOwnerType ownerType);
}
