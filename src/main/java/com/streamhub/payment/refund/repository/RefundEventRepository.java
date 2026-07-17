package com.streamhub.payment.refund.repository;

import com.streamhub.payment.refund.entity.RefundEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundEventRepository extends JpaRepository<RefundEventEntity,Long> {
}
