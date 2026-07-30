package com.streamhub.payment.wallet.entity;


import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.wallet.enums.WalletHoldStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "wallet_holds")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WalletHoldEntity extends BaseEntity {

    @Column(name = "wallet_hold_reference",nullable = false,unique = true,length = 26)
    private String walletHoldReference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "wallet_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_wallet_hold_wallet")
    )
    private WalletEntity wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "payout_id",
            foreignKey = @ForeignKey(name = "fk_wallet_hold_payout")
    )
    private PayoutEntity payout;

    @Column(name = "balance_before",nullable = false)
    private Long balanceBefore;

    @Column(name = "balance_after",nullable = false)
    private Long balanceAfter;

    @Column(name = "amount",nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private WalletHoldStatus status;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
}
