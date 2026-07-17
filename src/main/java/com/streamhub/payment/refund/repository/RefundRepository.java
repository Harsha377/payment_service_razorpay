package com.streamhub.payment.refund.repository;

import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.refund.entity.RefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface RefundRepository extends JpaRepository<RefundEntity ,Long> {


    @Query("""
    SELECT COALESCE(SUM(r.amount), 0)
    FROM RefundEntity r
    WHERE r.payment = :payment
      AND r.status = com.streamhub.payment.refund.enums.RefundStatus.SUCCESS
""")
    Long sumSuccessfulRefundAmountByPayment(
            @Param("payment") PaymentEntity payment
    );

   Optional<RefundEntity> findByRefundReference(String refundReference);

    List<RefundEntity> findAllByPayment(PaymentEntity payment);
    Optional<RefundEntity> findByGatewayRefundId(String gatewayRefundId);
}
