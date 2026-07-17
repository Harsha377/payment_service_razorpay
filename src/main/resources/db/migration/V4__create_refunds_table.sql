CREATE  TABLE  refunds(

    id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    refund_reference VARCHAR(26) NOT NULL ,
    payment_id BIGINT NOT NULL ,
    gateway_refund_id VARCHAR(255),
    amount BIGINT NOT NULL ,
    currency VARCHAR(3) NOT NULL ,
    reason VARCHAR(50) NOT NULL ,
    speed VARCHAR(30) NOT NULL ,
    initiator VARCHAR(30) NOT NULL ,
    status VARCHAR(30) NOT NULL ,
    gateway_status VARCHAR(50),
    metadata JSONB,
    gateway_response JSONB,
    gateway_notes JSONB,
    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL  DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_refunds_refund_reference UNIQUE (refund_reference),
    CONSTRAINT uk_refunds_gateway_refund_id UNIQUE (gateway_refund_id),
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE RESTRICT ,
    CONSTRAINT chk_refunds_amount CHECK ( amount>0 ),
    CONSTRAINT  chk_refunds_currency CHECK ( currency=UPPER(currency) AND char_length(currency)=3)
);

CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_status ON refunds(status);
CREATE INDEX idx_refunds_gateway_status ON refunds(gateway_status);
CREATE INDEX idx_refunds_processed_at ON refunds(processed_at);
CREATE INDEX idx_refunds_created_at ON refunds(created_at);