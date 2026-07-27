package com.streamhub.payment.payout.repository;

import com.streamhub.payment.payout.entity.PayoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayoutRepository extends JpaRepository<PayoutEntity,Long> {
    Optional<PayoutEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<PayoutEntity> findByGatewayPayoutId(String gatewayPayoutId);
}
