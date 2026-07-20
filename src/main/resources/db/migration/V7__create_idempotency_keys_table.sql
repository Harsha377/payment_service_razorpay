CREATE  TABLE idempotency_keys(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    idempotency_key VARCHAR(255) NOT NULL ,
    request_hash VARCHAR(64) NOT NULL ,
    resource_type VARCHAR(50) NOT NULL ,
    resource_reference VARCHAR(26) NOT NULL ,
    http_status INTEGER NOT NULL ,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL ,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_idempotency_keys_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_idempotency_keys_resource
ON idempotency_keys(resource_type,resource_reference);

CREATE INDEX idx_idempotency_keys_expires_at
ON idempotency_keys(expires_at);