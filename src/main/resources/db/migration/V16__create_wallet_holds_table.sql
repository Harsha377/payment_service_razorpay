CREATE TABLE wallet_holds(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    wallet_hold_reference VARCHAR(26) NOT NULL ,
    wallet_id BIGINT NOT NULL ,
    payout_id BIGINT,

    amount BIGINT NOT NULL ,
    balance_before BIGINT NOT NULL ,
    balance_after BIGINT NOT NULL ,

    status VARCHAR(30) NOT NULL ,

    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_wallet_hold_reference UNIQUE (wallet_hold_reference),

    CONSTRAINT fk_wallet_hold_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),

    CONSTRAINT fk_wallet_hold_payout FOREIGN KEY (payout_id) REFERENCES payouts(id),

    CONSTRAINT chk_wallet_hold_amount CHECK ( amount >0 )
);

CREATE INDEX idx_wallet_hold_reference
ON wallet_holds(wallet_hold_reference);

CREATE INDEX idx_wallet_hold_wallet
ON wallet_holds(wallet_id);

CREATE INDEX idx_wallet_hold_payout
ON wallet_holds(payout_id);

CREATE INDEX  idx_wallet_hold_status
ON wallet_holds(status);

CREATE INDEX idx_wallet_hold_created_at
ON wallet_holds(created_at);