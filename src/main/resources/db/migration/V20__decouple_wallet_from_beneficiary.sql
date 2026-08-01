-- Wallets no longer require a beneficiary to exist. A wallet is now
-- identified purely by (owner_reference, owner_type):
--   owner_type = 'CUSTOMER' -> owner_reference = customer_reference
--   owner_type = 'PROVIDER' -> owner_reference = beneficiary_reference
--   owner_type = 'PLATFORM' -> owner_reference = a fixed platform key
--
-- A Beneficiary (Razorpay contact + fund account) is still created and
-- linked separately, only when an actual bank payout needs to go out.

ALTER TABLE wallets
    ADD COLUMN owner_reference VARCHAR(26),
    ADD COLUMN owner_type VARCHAR(30);

-- Backfill: every existing wallet was created for a beneficiary/provider.
-- Use the beneficiary's owner_reference (the caller-supplied stable provider
-- ID) as the new wallet owner_reference - NOT beneficiary_reference, which is
-- a system-generated ULID that future payouts won't be able to reproduce
-- from the provider's stable ID alone.
UPDATE wallets w
SET owner_reference = b.owner_reference,
    owner_type       = 'PROVIDER'
    FROM beneficiaries b
WHERE w.beneficiary_id = b.id;

ALTER TABLE wallets
    ALTER COLUMN owner_reference SET NOT NULL,
ALTER COLUMN owner_type SET NOT NULL;

ALTER TABLE wallets
    ADD CONSTRAINT chk_wallet_owner_type
        CHECK (owner_type IN ('CUSTOMER', 'PROVIDER', 'PLATFORM'));

-- Drop the old beneficiary coupling
ALTER TABLE wallets DROP CONSTRAINT fk_wallet_beneficiary;
ALTER TABLE wallets DROP CONSTRAINT uk_wallet_beneficiary;
DROP INDEX IF EXISTS idx_wallet_beneficiary;
ALTER TABLE wallets DROP COLUMN beneficiary_id;

-- One wallet per owner
ALTER TABLE wallets
    ADD CONSTRAINT uk_wallet_owner UNIQUE (owner_reference, owner_type);

CREATE INDEX idx_wallet_owner
    ON wallets(owner_reference, owner_type);
