package com.streamhub.payment.settlement.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.settlement.enums.SettlementStatus;
import com.streamhub.payment.wallet.entity.WalletEntity;
import com.streamhub.payment.wallet.enums.WalletOwnerType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "settlements",
        indexes = {
                @Index(name = "idx_settlement_reference", columnList = "settlement_reference"),
                @Index(name = "idx_settlement_payment", columnList = "payment_id"),
                @Index(name = "idx_settlement_owner", columnList = "owner_reference, owner_type"),
                @Index(name = "idx_settlement_wallet", columnList = "wallet_id"),
                @Index(name = "idx_settlement_status", columnList = "status"),
                @Index(name = "idx_settlement_created_at", columnList = "created_at")
        }
)
public class SettlementEntity extends BaseEntity {


    @Column(name = "settlement_reference", nullable = false, unique = true, length = 26)
    private String settlementReference;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentEntity payment;

    /**
     * Who this settlement pays out to, e.g. a provider's beneficiaryReference.
     * Deliberately not a foreign key to BeneficiaryEntity - a settlement (and
     * the wallet credit it triggers) can happen before any beneficiary/payout
     * onboarding exists.
     */
    @Column(name = "owner_reference", nullable = false, length = 26)
    private String ownerReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private WalletOwnerType ownerType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    @Column(name = "gross_amount", nullable = false)
    private Long grossAmount;

    @Column(name = "commission_amount", nullable = false)
    private Long commissionAmount;

    @Column(name = "net_amount", nullable = false)
    private Long netAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SettlementStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @OneToMany(
            mappedBy = "settlement",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<SettlementEventEntity> settlementEvents = new ArrayList<>();

}