package com.streamhub.payment.settlement.repository;

import com.streamhub.payment.settlement.entity.SettlementEntity;
import com.streamhub.payment.settlement.entity.SettlementEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementEventRepository extends JpaRepository<SettlementEventEntity, Long> {

    List<SettlementEventEntity> findBySettlementOrderByCreatedAtAsc(SettlementEntity settlement);
}