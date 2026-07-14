package com.streamhub.payment.payment.repository;

import com.streamhub.payment.payment.entity.PaymentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentEventRepository extends JpaRepository<PaymentEventEntity,Long> {
    List<PaymentEventEntity> findByPaymentIdOrderByCreatedAtAsc(Long paymentId);
}
