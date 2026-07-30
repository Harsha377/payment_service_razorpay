package com.streamhub.payment.wallet.repository;

import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.enums.WalletStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<WalletEntity,Long> {

    Optional<WalletEntity> findByWalletReference(String walletReference);
    Optional<WalletEntity> findByBeneficiary(BeneficiaryEntity beneficiary);
    Optional<WalletEntity> findByBeneficiaryAndStatus(BeneficiaryEntity beneficiary, WalletStatus status);
    boolean existsByBeneficiary(BeneficiaryEntity beneficiary);
}
