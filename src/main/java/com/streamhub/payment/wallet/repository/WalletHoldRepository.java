package com.streamhub.payment.wallet.repository;

import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.entity.WalletHoldEntity;
import com.streamhub.payment.wallet.enums.WalletHoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletHoldRepository extends JpaRepository<WalletHoldEntity,Long> {

    Optional<WalletHoldEntity> findByWalletHoldReference(String walletHoldReference);

    List<WalletHoldEntity> findByWalletAndStatus(WalletEntity wallet, WalletHoldStatus status);

    Optional<WalletHoldEntity> findByPayout(PayoutEntity payout);
}
