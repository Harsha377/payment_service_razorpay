package com.streamhub.payment.payout.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.common.entity.EventBaseEntity;
import com.streamhub.payment.payout.enums.PayoutEventSource;
import com.streamhub.payment.payout.enums.PayoutEventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "payout_events",
        uniqueConstraints = {

        @UniqueConstraint(
                name = "uk_payout_event_reference",
                columnNames = "payout_event_reference"
        )}
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutEventEntity extends EventBaseEntity {

    @Column(name = "payout_event_reference",nullable = false,length = 26)
    private String payoutEventReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "payout_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payout_event_payout")
    )
    private PayoutEntity payout;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type",nullable = false,length = 50)
    private PayoutEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_source",nullable = false,length = 50)
    private PayoutEventSource eventSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data",nullable = false,columnDefinition = "jsonb")
    private JsonNode eventData;
}
