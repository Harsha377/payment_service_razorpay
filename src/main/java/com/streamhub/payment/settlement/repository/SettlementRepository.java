package com.streamhub.payment.settlement.repository;

import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.settlement.entity.SettlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<SettlementEntity, Long> {

    Optional<SettlementEntity> findBySettlementReference(String settlementReference);

    Optional<SettlementEntity> findByPayment(PaymentEntity payment);

    boolean existsByPayment(PaymentEntity payment);
}