CREATE TABLE refund_events(

    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY ,
    refund_event_reference VARCHAR(26) NOT NULL ,
    refund_id BIGINT NOT NULL ,
    event_type VARCHAR(50) NOT NULL ,
    event_source VARCHAR(50) NOT NULL ,
    event_data JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_refund_events_reference UNIQUE (refund_event_reference),
    CONSTRAINT fk_refund_events_refund FOREIGN KEY (refund_id)
                          REFERENCES refunds(id)
                          ON DELETE RESTRICT
);

CREATE INDEX idx_refund_events_refund_id ON refund_events(refund_id);
CREATE INDEX idx_refund_events_event_type ON refund_events(event_type);
CREATE INDEX idx_refund_events_event_source ON refund_events(event_source);
CREATE INDEX idx_refund_events_created_at ON refund_events(created_at);