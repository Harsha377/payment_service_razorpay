-- ============================================
-- Add Reconciliation Fields to Payouts
-- ============================================

ALTER TABLE payouts
    ADD COLUMN utr VARCHAR(100),
    ADD COLUMN fees BIGINT,
    ADD COLUMN tax BIGINT,
    ADD COLUMN processed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN failed_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN reversed_at TIMESTAMP WITH TIME ZONE;