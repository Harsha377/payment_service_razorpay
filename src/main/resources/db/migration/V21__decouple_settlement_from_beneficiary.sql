-- Settlements move a captured payment's split into a wallet identified by
-- (owner_reference, owner_type), same model as wallets - no beneficiary
-- record is required for a settlement/wallet credit to happen.

ALTER TABLE settlements
    ADD COLUMN owner_reference VARCHAR(26),
    ADD COLUMN owner_type VARCHAR(30);

UPDATE settlements s
SET owner_reference = b.owner_reference,
    owner_type       = 'PROVIDER'
    FROM beneficiaries b
WHERE s.beneficiary_id = b.id;

ALTER TABLE settlements
    ALTER COLUMN owner_reference SET NOT NULL,
ALTER COLUMN owner_type SET NOT NULL;

ALTER TABLE settlements
    ADD CONSTRAINT chk_settlement_owner_type
        CHECK (owner_type IN ('CUSTOMER', 'PROVIDER', 'PLATFORM'));

ALTER TABLE settlements DROP CONSTRAINT fk_settlement_beneficiary;
DROP INDEX IF EXISTS idx_settlement_beneficiary;
ALTER TABLE settlements DROP COLUMN beneficiary_id;

CREATE INDEX idx_settlement_owner
    ON settlements(owner_reference, owner_type);
