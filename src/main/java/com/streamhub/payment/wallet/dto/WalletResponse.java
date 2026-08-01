package com.streamhub.payment.wallet.dto;

import com.streamhub.payment.wallet.enums.WalletOwnerType;
import com.streamhub.payment.wallet.enums.WalletStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WalletResponse {

    String walletReference;

    String ownerReference;

    WalletOwnerType ownerType;

    Long availableBalance;

    Long pendingBalance;

    Long totalCredited;

    Long totalDebited;

    String currency;

    WalletStatus status;
}
