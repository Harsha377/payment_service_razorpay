package com.streamhub.payment.refund.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.refund.entity.RefundEntity;
import com.streamhub.payment.refund.enums.RefundEventSource;
import com.streamhub.payment.refund.enums.RefundEventType;

public interface RefundEventService {
    void recordEvent(
            RefundEntity refund,
            RefundEventType eventType,
            RefundEventSource eventSource,
            JsonNode eventData
    );
}
