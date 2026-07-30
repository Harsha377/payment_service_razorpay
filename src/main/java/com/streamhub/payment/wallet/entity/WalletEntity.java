package com.streamhub.payment.wallet.entity;

import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.wallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletEntity  extends BaseEntity {

    @Column(name = "wallet_reference",nullable = false,unique = true,length = 26)
    private String walletReference;

    @OneToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "beneficiary_id",nullable = false , foreignKey = @ForeignKey(name = "fk_wallet_beneficiary"))
    private BeneficiaryEntity beneficiary;

    @Column(name = "available_balance",nullable = false)
    private Long availableBalance;

    @Column(name = "pending_balance",nullable = false)
    private Long pendingBalance;

    @Column(name = "total_credited",nullable = false)
    private Long totalCredited;

    @Column(name = "total_debited",nullable = false)
    private Long totalDebited;

    @Column(name = "currency",nullable = false,length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 30)
    private WalletStatus status;


}
