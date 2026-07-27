CREATE TABLE payouts(

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    payout_reference VARCHAR(26) NOT NULL ,
    idempotency_key VARCHAR(100) NOT NULL ,
    beneficiary_id BIGINT NOT NULL ,
    fund_account_id BIGINT NOT NULL ,
    gateway_name VARCHAR(50) NOT NULL ,
    gateway_payout_id VARCHAR(100),
    amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    mode VARCHAR(30) NOT NULL ,
    purpose VARCHAR(50) NOT NULL ,
    status VARCHAR(30) NOT NULL ,
    failure_reason VARCHAR(500),
    gateway_response JSONB,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_payout_reference UNIQUE (payout_reference),
    CONSTRAINT uk_payout_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT uk_gateway_payout_id UNIQUE (gateway_payout_id),
    CONSTRAINT fk_payout_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id),
    CONSTRAINT fk_payout_fund_account FOREIGN KEY (fund_account_id) REFERENCES fund_accounts(id),
    CONSTRAINT chk_payout_amount CHECK ( amount > 0 ),
    CONSTRAINT chk_payout_currency CHECK ( char_length(currency) =3 )

);


CREATE INDEX idx_payout_reference
    ON payouts(payout_reference);

CREATE INDEX idx_gateway_payout_id
    ON payouts(gateway_payout_id);

CREATE INDEX idx_payout_status
    ON payouts(status);

CREATE INDEX idx_payout_beneficiary
    ON payouts(beneficiary_id);

CREATE INDEX idx_payout_fund_account
    ON payouts(fund_account_id);

CREATE INDEX idx_payout_created_at
    ON payouts(created_at);
