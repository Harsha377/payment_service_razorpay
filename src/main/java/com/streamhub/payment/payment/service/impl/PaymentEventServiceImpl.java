package com.streamhub.payment.payment.service.impl;

import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.payment.dto.response.PaymentEventResponse;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.entity.PaymentEventEntity;
import com.streamhub.payment.payment.enums.PaymentEventSource;
import com.streamhub.payment.payment.enums.PaymentEventType;
import com.streamhub.payment.payment.repository.PaymentEventRepository;
import com.streamhub.payment.payment.service.PaymentEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentEventServiceImpl implements PaymentEventService {
    private final PaymentEventRepository paymentEventRepository;


    @Override
    public void recordPaymentSuccess(PaymentEntity payment) {

        PaymentEventEntity event = new PaymentEventEntity();
        event.setPaymentEventReference(UlidGenerator.generate());
        event.setPayment(payment);
        event.setEventType(PaymentEventType.PAYMENT_SUCCESS);
        event.setEventSource(PaymentEventSource.SYSTEM);
        paymentEventRepository.save(event);
    }
    @Override
    public void recordPaymentCreated(PaymentEntity payment) {
        PaymentEventEntity event=new PaymentEventEntity();
        event.setPaymentEventReference(UlidGenerator.generate());
        event.setPayment(payment);
        event.setEventType(PaymentEventType.PAYMENT_CREATED);
        event.setEventSource(PaymentEventSource.SYSTEM);
        event.setEventData(null);
        paymentEventRepository.save(event);

    }

    @Override
    public List<PaymentEventResponse> getPaymentEvents(String paymentReference) {
        return List.of();
    }

    @Override
    public void recordPaymentFailed(PaymentEntity payment) {

    }

    @Override
    public void recordPaymentExpired(PaymentEntity payment) {

    }

    @Override
    public void recordPaymentAuthorized(PaymentEntity payment) {

    }
}
