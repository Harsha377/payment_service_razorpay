CREATE TABLE  beneficiaries (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    beneficiary_reference VARCHAR(26) NOT NULL ,
    owner_reference VARCHAR(26) NOT NULL ,
    owner_type VARCHAR(30) NOT NULL ,
    name VARCHAR(150) NOT NULL ,
    email VARCHAR(255) NOT NULL ,
    contact_number VARCHAR(20) NOT NULL ,
    gateway_name VARCHAR(50) NOT NULL ,
    gateway_contact_id VARCHAR(100),
    status VARCHAR(30) NOT NULL ,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_beneficiary_reference UNIQUE (beneficiary_reference),
    CONSTRAINT uk_beneficiary_owner UNIQUE (owner_reference,owner_type),
    CONSTRAINT uk_gateway_contact_id UNIQUE (gateway_contact_id)
);

-- Owner lookup (most frequent)
CREATE INDEX idx_beneficiary_owner
    ON beneficiaries(owner_reference, owner_type);

-- Gateway contact lookup
CREATE INDEX idx_beneficiary_gateway_contact
    ON beneficiaries(gateway_contact_id);