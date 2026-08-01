package com.streamhub.payment.wallet.entity;

import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.wallet.enums.WalletOwnerType;
import com.streamhub.payment.wallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_wallet_owner",
                        columnNames = {"owner_reference", "owner_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletEntity extends BaseEntity {

    @Column(name = "wallet_reference", nullable = false, unique = true, length = 26)
    private String walletReference;

    /**
     * External reference of whoever owns this wallet: customerReference for a
     * CUSTOMER wallet, beneficiaryReference for a PROVIDER wallet, etc.
     * Deliberately NOT a foreign key to BeneficiaryEntity - a wallet can exist
     * and be credited/debited without any beneficiary/payout onboarding ever
     * having happened.
     */
    @Column(name = "owner_reference", nullable = false, length = 26)
    private String ownerReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private WalletOwnerType ownerType;

    @Column(name = "available_balance", nullable = false)
    private Long availableBalance;

    @Column(name = "pending_balance", nullable = false)
    private Long pendingBalance;

    @Column(name = "total_credited", nullable = false)
    private Long totalCredited;

    @Column(name = "total_debited", nullable = false)
    private Long totalDebited;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private WalletStatus status;

}
