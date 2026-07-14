package com.streamhub.payment.payment.repository;

import com.streamhub.payment.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity,Long> {
    Optional<PaymentEntity> findByPaymentReference(String paymentReference);

    Optional<PaymentEntity> findByGatewayOrderId(String gatewayOrderId);

    Optional<PaymentEntity> findByGatewayPaymentId(String gatewayPaymentId);
}
