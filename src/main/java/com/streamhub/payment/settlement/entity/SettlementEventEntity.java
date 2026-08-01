package com.streamhub.payment.settlement.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.EventBaseEntity;
import com.streamhub.payment.settlement.enums.SettlementEventSource;
import com.streamhub.payment.settlement.enums.SettlementEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(
        name = "settlement_events",
        indexes = {
                @Index(name = "idx_settlement_event_reference", columnList = "settlement_event_reference"),
                @Index(name = "idx_settlement_event_settlement", columnList = "settlement_id"),
                @Index(name = "idx_settlement_event_type", columnList = "event_type"),
                @Index(name = "idx_settlement_event_created_at", columnList = "created_at")
        }
)
public class SettlementEventEntity extends EventBaseEntity {


    @Column(name = "settlement_event_reference", nullable = false, unique = true, length = 26)
    private String settlementEventReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    private SettlementEntity settlement;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private SettlementEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_source", nullable = false, length = 30)
    private SettlementEventSource eventSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private JsonNode eventData;

}