package com.streamhub.payment.payment.service.impl;

import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ConflictException;
import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.gateway.dto.request.GatewayOrderRequest;
import com.streamhub.payment.gateway.dto.response.GatewayOrderResponse;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.payment.dto.request.CreatePaymentIntentRequest;
import com.streamhub.payment.payment.dto.response.CreatePaymentIntentResponse;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.entity.PaymentIntentEntity;
import com.streamhub.payment.payment.enums.PaymentIntentStatus;
import com.streamhub.payment.payment.mapper.PaymentMapper;
import com.streamhub.payment.payment.repository.PaymentIntentRepository;
import com.streamhub.payment.payment.service.PaymentEventService;
import com.streamhub.payment.payment.service.PaymentIntentService;
import com.streamhub.payment.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentIntentServiceImpl implements PaymentIntentService {
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentService paymentService;
    private final PaymentEventService paymentEventService;
    private final RazorpayService razorpayService;
    private final PaymentMapper paymentMapper;



    @Override
    public CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request) {
        validateRequest(request);
        PaymentIntentEntity paymentIntent=createPaymentIntentEntity(request);
        paymentIntent=paymentIntentRepository.save(paymentIntent);
        GatewayOrderResponse gatewayOrder =
                razorpayService.createOrder(buildGatewayOrderRequest(paymentIntent));
        PaymentEntity payment = paymentService.createPayment(paymentIntent,gatewayOrder);
        paymentEventService.recordPaymentCreated(payment);
        return paymentMapper.toCreatePaymentIntentResponse(paymentIntent,payment,gatewayOrder);

    }

    private void validateRequest(CreatePaymentIntentRequest request){
        if (!"INR".equals(request.getCurrency())){
            throw new BadRequestException( "Currently only INR currency is supported.");
        }
        paymentIntentRepository.findByPaymentIntentReference(request.getOrderReference())
                .ifPresent(paymentIntent ->{
                    throw new ConflictException( "A payment intent already exists for this order.");
                });
    }

    private PaymentIntentEntity createPaymentIntentEntity(CreatePaymentIntentRequest request){
        PaymentIntentEntity entity=new PaymentIntentEntity();
        entity.setPaymentIntentReference(UlidGenerator.generate());
        entity.setOrderReference(request.getOrderReference());
        entity.setCustomerReference(request.getCustomerReference());
        entity.setAmount(request.getAmount());
        entity.setCurrency(request.getCurrency());
        entity.setDescription(request.getDescription());
        entity.setStatus(PaymentIntentStatus.CREATED);
        entity.setExpiresAt(OffsetDateTime.now().plusMinutes(15));
        return entity;
    }

    private GatewayOrderRequest buildGatewayOrderRequest(PaymentIntentEntity paymentIntent){
        return GatewayOrderRequest.builder()
                .receipt(paymentIntent.getPaymentIntentReference())
                .amount(paymentIntent.getAmount())
                .currency(paymentIntent.getCurrency())
                .build();
    }
}
