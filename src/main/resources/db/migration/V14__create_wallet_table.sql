CREATE TABLE wallets (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    wallet_reference VARCHAR(26) NOT NULL ,
    beneficiary_id BIGINT NOT NULL ,
    available_balance BIGINT NOT NULL DEFAULT 0,
    pending_balance BIGINT NOT NULL DEFAULT 0,
    total_credited BIGINT NOT NULL DEFAULT 0,
    total_debited BIGINT NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL ,
    status VARCHAR(30) NOT NULL ,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_wallet_reference UNIQUE (wallet_reference),

     --       One Wallet Per Beneficiary
    CONSTRAINT uk_wallet_beneficiary UNIQUE (beneficiary_id),

    CONSTRAINT fk_wallet_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id),

    CONSTRAINT chk_available_balance CHECK ( available_balance >=0 ),

    CONSTRAINT chk_pending_balance CHECK ( pending_balance >=0 ),

    CONSTRAINT chk_total_credited CHECK ( total_credited >=0 ),

    CONSTRAINT  chk_total_debited CHECK ( total_debited >=0 ),

    CONSTRAINT chk_wallet_currency CHECK ( char_length(currency) =3 )
);


CREATE INDEX idx_wallet_reference
ON wallets(wallet_reference);

CREATE INDEX idx_wallet_beneficiary
ON wallets(beneficiary_id);

CREATE INDEX idx_wallet_status
ON wallets(status);

CREATE INDEX idx_wallet_created_at
ON wallets(created_at);