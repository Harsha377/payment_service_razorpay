CREATE TABLE fund_accounts(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    fund_account_reference VARCHAR(26) NOT NULL ,
    beneficiary_id BIGINT NOT NULL ,
    fund_account_type VARCHAR(30) NOT NULL ,
    account_holder_name VARCHAR(150),
    account_number VARCHAR(50),
    ifsc_code VARCHAR(20),
    vpa VARCHAR(100),
    gateway_fund_account_id VARCHAR(100),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL ,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_fund_account_reference UNIQUE (fund_account_reference),
    CONSTRAINT uk_gateway_fund_account UNIQUE (gateway_fund_account_id),
    CONSTRAINT fk_fund_account_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiaries(id)
);

-- Beneficiary -> Fund Accounts
CREATE INDEX idx_fund_account_beneficiary
    ON fund_accounts(beneficiary_id);

-- Gateway Fund Account lookup
CREATE INDEX idx_fund_account_gateway
    ON fund_accounts(gateway_fund_account_id);

CREATE INDEX idx_fund_account_beneficiary_type
    ON fund_accounts (beneficiary_id, fund_account_type);

-- Frequently used during payout screen
CREATE INDEX idx_fund_account_default
    ON fund_accounts(beneficiary_id, is_default);

-- ============================================================
-- Business Rule:
-- One BANK_ACCOUNT per beneficiary.
--
-- Future:
-- If multiple bank accounts are supported,
-- simply DROP this index.
-- No code or schema changes are required.
-- ============================================================
CREATE UNIQUE INDEX uk_bank_account_per_beneficiary
    ON fund_accounts (beneficiary_id)
    WHERE fund_account_type = 'BANK_ACCOUNT';

-- ============================================================
-- Business Rule:
-- One VPA per beneficiary.
--
-- Future:
-- If multiple VPAs are supported,
-- simply DROP this index.
-- No code or schema changes are required.
-- ============================================================

CREATE UNIQUE INDEX uk_vpa_per_beneficiary
    ON fund_accounts (beneficiary_id)
    WHERE fund_account_type = 'VPA';