package com.streamhub.payment.payment.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.payment.enums.PaymentFlowType;
import com.streamhub.payment.payment.enums.PaymentIntentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

//(access = AccessLevel.PROTECTED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "payment_intents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payment_intents_reference",
                        columnNames = "payment_intent_reference"
                )
        }
)
public class PaymentIntentEntity extends BaseEntity {

    @Column(name = "payment_intent_reference", nullable = false, length = 26)
    private String paymentIntentReference;

    @Column(name = "order_reference", nullable = false, length = 26)
    private String orderReference;

    @Column(name = "customer_reference", nullable = false, length = 26)
    private String customerReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_flow_type", nullable = false, length = 20)
    private PaymentFlowType paymentFlowType;

    @Column(name = "settlement_beneficiary_reference", length = 26)
    private String settlementBeneficiaryReference;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentIntentStatus status;

    @Column(name = "description", length = 255)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

}