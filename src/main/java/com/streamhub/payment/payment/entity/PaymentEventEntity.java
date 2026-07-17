package com.streamhub.payment.payment.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.EventBaseEntity;
import com.streamhub.payment.payment.enums.PaymentEventSource;
import com.streamhub.payment.payment.enums.PaymentEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_events")
public class PaymentEventEntity extends EventBaseEntity {

    @Column(name = "payment_event_reference", nullable = false, length = 26)
    private String paymentEventReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "payment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payment_events_payments")
    )
    private PaymentEntity payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private PaymentEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_source", nullable = false, length = 50)
    private PaymentEventSource eventSource;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private JsonNode eventData;

}