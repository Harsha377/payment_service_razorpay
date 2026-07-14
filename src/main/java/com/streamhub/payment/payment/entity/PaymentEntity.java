package com.streamhub.payment.payment.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.payment.enums.PaymentGateway;
import com.streamhub.payment.payment.enums.PaymentMethod;
import com.streamhub.payment.payment.enums.PaymentStatus;
import jakarta.persistence.*;
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
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payments_reference",
                        columnNames = "payment_reference"
                ),
                @UniqueConstraint(
                        name = "uk_payments_gateway_payment_id",
                        columnNames = "gateway_payment_id"
                )
        }
)
public class PaymentEntity extends BaseEntity {

    @Column(name = "payment_reference", nullable = false, length = 26)
    private String paymentReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_intent_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_payment_intents"))
    private PaymentIntentEntity paymentIntent;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_name", nullable = false, length = 50)
    private PaymentGateway gatewayName;

    @Column(name = "gateway_order_id", nullable = false, length = 255)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", length = 255)
    private String gatewayPaymentId;

    @Column(name = "gateway_signature", length = 512)
    private String gatewaySignature;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "payment_method", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "gateway_status", length = 50)
    private String gatewayStatus;


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private JsonNode gatewayResponse;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

}