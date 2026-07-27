package com.streamhub.payment.payout.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.entity.FundAccountEntity;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.payment.enums.PaymentGateway;
import com.streamhub.payment.payout.enums.PayoutMode;
import com.streamhub.payment.payout.enums.PayoutPurpose;
import com.streamhub.payment.payout.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name="payouts",
        uniqueConstraints  = {
                @UniqueConstraint(
                        name = "uk_payout_reference",
                        columnNames = "payout_reference"
                ),
                @UniqueConstraint(
                        name = "uk_payout_idempotency_key",
                        columnNames = "idempotency_key"
                ),
                @UniqueConstraint(
                        name = "uk_gateway_payout_id",
                        columnNames = "gateway_payout_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutEntity extends BaseEntity {

    @Column(name = "payout_reference",nullable = false,length = 26)
    private String payoutReference;

    @Column(name = "idempotency_key",nullable = false,length = 100)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beneficiary_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payout_beneficiary"))
    private BeneficiaryEntity beneficiary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "fund_account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payout_fund_account")
    )
    private FundAccountEntity fundAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_name",nullable = false,length = 50)
    private PaymentGateway gatewayName;

    @Column(name = "gateway_payout_id",length = 100)
    private String gatewayPayoutId;


    @Column(name = "amount",nullable = false)
    private Long amount;

    @Column(name = "currency",nullable = false,length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode",nullable = false,length = 30)
    private PayoutMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose",nullable = false,length = 50)
    private PayoutPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 30)
    private PayoutStatus status;

    @Column(name = "failure_reason",length = 500)
    private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response",columnDefinition = "jsonb")
    private JsonNode gatewayResponse;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata",columnDefinition = "jsonb")
    private JsonNode metadata;

    @Column(name = "utr")
    private String utr;

    @Column(name = "fees")
    private Long fees;

    @Column(name = "tax")
    private Long tax;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "failed_at")
    private OffsetDateTime failedAt;

    @Column(name = "reversed_at")
    private OffsetDateTime reversedAt;

}
