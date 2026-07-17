package com.streamhub.payment.refund.entity;


import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.refund.enums.RefundInitiator;
import com.streamhub.payment.refund.enums.RefundReason;
import com.streamhub.payment.refund.enums.RefundSpeed;
import com.streamhub.payment.refund.enums.RefundStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "refunds",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_refunds_refund_reference",
                        columnNames = "refund_reference"
                ),
                @UniqueConstraint(
                        name = "uk_refunds_gateway_refund_id",
                        columnNames = "gateway_refund_id"
                )
        }
)
public class RefundEntity extends BaseEntity {
    @Column(name = "refund_reference",nullable = false,length = 26)
    private String refundReference;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "payment_id",nullable = false,foreignKey = @ForeignKey(name = "fk_refunds_payment"))
    private PaymentEntity payment;

    @Column(name = "gateway_refund_id",length = 255)
    private String gatewayRefundId;

    @Column(name = "amount", nullable = false)
    private Long amount;
    @Column(name = "currency", nullable = false,length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false,length = 50)
    private RefundReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "speed", nullable = false,length = 30)
    private RefundSpeed speed;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiator", nullable = false, length = 30)
    private RefundInitiator initiator;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false,length = 30)
    private RefundStatus status;

    @Column(name = "gateway_status",length = 50)
    private String gatewayStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "gateway_response",
            columnDefinition = "jsonb"
    )
    private JsonNode gatewayResponse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "gateway_notes",
            columnDefinition = "jsonb"
    )
    private JsonNode gatewayNotes;
    @Column(name = "processed_at")
    private OffsetDateTime processedAt;
}
