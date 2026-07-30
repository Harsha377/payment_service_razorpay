CREATE TABLE wallet_transactions(

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    wallet_transaction_reference VARCHAR(26) NOT NULL ,
    wallet_id BIGINT NOT NULL ,
    transaction_type VARCHAR(30) NOT NULL ,
    transaction_source VARCHAR(30) NOT NULL ,
    amount BIGINT NOT NULL ,
    balance_before BIGINT NOT NULL ,
    balance_after BIGINT NOT NULL ,
    reference_type VARCHAR(30),
    reference_id VARCHAR(26),
    description VARCHAR(500),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_wallet_transaction_reference UNIQUE (wallet_transaction_reference),

    CONSTRAINT fk_wallet_transaction_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id),

    CONSTRAINT chk_wallet_transaction_amount CHECK ( amount > 0 ) ,

    CONSTRAINT chk_balance_before CHECK ( balance_before >=0 ),

    CONSTRAINT  chk_balance_after CHECK ( balance_after >=0 )
);

CREATE INDEX idx_wallet_transaction_reference
ON wallet_transactions(wallet_transaction_reference);

CREATE INDEX idx_wallet_transaction_wallet
ON wallet_transactions(wallet_id);

CREATE INDEX idx_wallet_transaction_reference_type
ON wallet_transactions(reference_type);

CREATE INDEX idx_wallet_transaction_reference_id
ON wallet_transactions(reference_id);

CREATE INDEX idx_wallet_transaction_created_at
ON wallet_transactions(created_at);