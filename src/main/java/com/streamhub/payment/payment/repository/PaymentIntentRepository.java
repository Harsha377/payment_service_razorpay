package com.streamhub.payment.payment.repository;

import com.streamhub.payment.payment.entity.PaymentIntentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository <PaymentIntentEntity,Long>{
    Optional<PaymentIntentEntity> findByPaymentIntentReference(String paymentIntentReference);

    Optional<PaymentIntentEntity> findByOrderReference(String orderReference);
}
