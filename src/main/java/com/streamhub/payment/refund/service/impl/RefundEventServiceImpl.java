package com.streamhub.payment.refund.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.refund.entity.RefundEntity;
import com.streamhub.payment.refund.entity.RefundEventEntity;
import com.streamhub.payment.refund.enums.RefundEventSource;
import com.streamhub.payment.refund.enums.RefundEventType;
import com.streamhub.payment.refund.repository.RefundEventRepository;
import com.streamhub.payment.refund.service.RefundEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundEventServiceImpl implements RefundEventService {
    private final RefundEventRepository refundEventRepository;

    @Override
    public void recordEvent(RefundEntity refund, RefundEventType eventType, RefundEventSource eventSource, JsonNode eventData) {
        RefundEventEntity refundEvent = buildRefundEvent(refund, eventType, eventSource, eventData);
        refundEventRepository.save(refundEvent);

        log.info("Refund event recorded successfully. Refund Reference: {}, Event Type: {}",
                refund.getRefundReference(), eventType);
    }

    private RefundEventEntity buildRefundEvent(
            RefundEntity refund,
            RefundEventType eventType,
            RefundEventSource eventSource,
            JsonNode eventData
    ){
        RefundEventEntity refundEvent=new RefundEventEntity();
        refundEvent.setRefundEventReference(UlidGenerator.generate());
        refundEvent.setRefund(refund);
        refundEvent.setEventType(eventType);
        refundEvent.setEventSource(eventSource);
        refundEvent.setEventData(eventData);
        return refundEvent;
    }
}
