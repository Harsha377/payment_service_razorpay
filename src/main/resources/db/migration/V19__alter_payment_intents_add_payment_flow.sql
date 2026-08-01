ALTER TABLE payment_intents
ADD COLUMN payment_flow_type VARCHAR(20) NOT NULL DEFAULT 'DIRECT';

ALTER TABLE payment_intents
ADD COLUMN settlement_beneficiary_reference VARCHAR(26);