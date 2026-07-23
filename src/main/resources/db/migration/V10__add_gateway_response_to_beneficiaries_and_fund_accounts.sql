-- ============================================================
-- Add gateway_response to beneficiaries
-- Stores the complete Razorpay Contact API response.
-- ============================================================

ALTER TABLE beneficiaries
ADD COLUMN gateway_response JSONB;

-- ============================================================
-- Add gateway_response to fund_accounts
-- Stores the complete Razorpay Fund Account API response.
-- ============================================================

ALTER TABLE fund_accounts
ADD COLUMN gateway_response JSONB;