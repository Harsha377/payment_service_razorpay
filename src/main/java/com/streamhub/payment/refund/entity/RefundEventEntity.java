package com.streamhub.payment.refund.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.EventBaseEntity;
import com.streamhub.payment.refund.enums.RefundEventSource;
import com.streamhub.payment.refund.enums.RefundEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "refund_events",uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_refund_events_reference",
                columnNames = "refund_event_reference"
        )}
)
public class RefundEventEntity extends EventBaseEntity {

    @Column(name = "refund_event_reference",nullable = false,length = 26)
    private String refundEventReference;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "refund_id",nullable = false,foreignKey = @ForeignKey(name = "fk_refund_events_refund"))
    private RefundEntity refund;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type",nullable = false,length = 50)
    private RefundEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_source",nullable = false,length = 50)
    private RefundEventSource eventSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data",columnDefinition = "jsonb")
    private JsonNode eventData;
}
