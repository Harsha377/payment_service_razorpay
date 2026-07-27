package com.streamhub.payment.payout.repository;

import com.streamhub.payment.payout.entity.PayoutEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayoutEventRepository extends JpaRepository<PayoutEventEntity,Long> {
}
