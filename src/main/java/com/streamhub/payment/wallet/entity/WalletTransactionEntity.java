package com.streamhub.payment.wallet.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.entity.EventBaseEntity;
import com.streamhub.payment.wallet.enums.WalletReferenceType;
import com.streamhub.payment.wallet.enums.WalletTransactionSource;
import com.streamhub.payment.wallet.enums.WalletTransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionEntity extends EventBaseEntity {

    @Column(name = "wallet_transaction_reference",nullable = false,unique = true,length = 26)
    private String walletTransactionReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "wallet_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_wallet_transaction_wallet")
    )
    private WalletEntity wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type",nullable = false,length = 30)
    private WalletTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_source",nullable = false,length = 30)
    private WalletTransactionSource transactionSource;

    @Column(name = "amount",nullable = false)
    private Long amount;

    @Column(name = "balance_before",nullable = false)
    private Long balanceBefore;

    @Column(name = "balance_after",nullable = false)
    private Long balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type",length = 30)
    private WalletReferenceType referenceType;

    @Column(name = "reference_id",length = 26)
    private String referenceId;

    @Column(name = "description",length = 500)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata",columnDefinition = "jsonb")
    private JsonNode metadata;
}
