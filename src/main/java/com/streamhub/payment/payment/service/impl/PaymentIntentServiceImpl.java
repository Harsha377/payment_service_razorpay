package com.streamhub.payment.payment.service.impl;

import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ConflictException;
import com.streamhub.payment.common.exception.ResourceNotFoundException;
import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.gateway.dto.request.GatewayOrderRequest;
import com.streamhub.payment.gateway.dto.response.GatewayOrderResponse;
import com.streamhub.payment.gateway.config.properties.RazorpayProperties;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.idempotency.config.HashGenerator;
import com.streamhub.payment.idempotency.entity.IdempotencyKeyEntity;
import com.streamhub.payment.idempotency.enums.ResourceType;
import com.streamhub.payment.idempotency.service.IdempotencyService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentIntentServiceImpl implements PaymentIntentService {
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentService paymentService;
    private final PaymentEventService paymentEventService;
    private final RazorpayService razorpayService;
    private final PaymentMapper paymentMapper;
    private final IdempotencyService idempotencyService;
    private final HashGenerator hashGenerator;
    private final RazorpayProperties razorpayProperties;


    @Override
    public CreatePaymentIntentResponse createPaymentIntent(String idempotencyKey, CreatePaymentIntentRequest request) {
        log.info("Creating payment intent. Order Reference: {}, Idempotency Key: {}",
                request.getOrderReference(), idempotencyKey);
        // Validate Request
        validateRequest(request);

        // Generate Request Hash
        String requestHash = hashGenerator.generateSha256(request);

        // Check Existing Idempotency Key
        Optional<IdempotencyKeyEntity> existingIdempotency =
                idempotencyService.findByIdempotencyKey(idempotencyKey);

        if (existingIdempotency.isPresent()) {

            if (!idempotencyService.isExpired(existingIdempotency.get())) {

                idempotencyService.validateRequest(existingIdempotency.get(), requestHash);
                return getExistingPaymentIntentResponse(existingIdempotency.get());
            }

            log.info("Expired idempotency key found. Removing expired record.");
            idempotencyService.delete(existingIdempotency.get());
        }
        // Create Payment Intent
        PaymentIntentEntity paymentIntent = createPaymentIntentEntity(request);
        paymentIntent = paymentIntentRepository.save(paymentIntent);
        log.info("Payment Intent created. Reference: {}", paymentIntent.getPaymentIntentReference());

        // Create Razorpay Order
        GatewayOrderResponse gatewayOrder =
                razorpayService.createOrder(buildGatewayOrderRequest(paymentIntent));

        // 6. Create Payment
        PaymentEntity payment = paymentService.createPayment(paymentIntent, gatewayOrder);
        // 7. Record Payment Created Event
        paymentEventService.recordPaymentCreated(payment);
        // 8. Save Idempotency Record
        idempotencyService.save(
                idempotencyKey, requestHash, ResourceType.PAYMENT_INTENT,
                paymentIntent.getPaymentIntentReference(),
                HttpStatus.CREATED.value()
        );
        log.info("Idempotency key saved. Payment Intent Reference: {}",
                paymentIntent.getPaymentIntentReference());
        // 9. Return Response
        return paymentMapper.toCreatePaymentIntentResponse(paymentIntent, payment, gatewayOrder);

    }

    private CreatePaymentIntentResponse getExistingPaymentIntentResponse(
            IdempotencyKeyEntity idempotencyKey) {

        if (idempotencyKey.getResourceType() != ResourceType.PAYMENT_INTENT) {
            throw new IllegalStateException(
                    "Invalid resource type for idempotent payment request.");
        }

        // Load existing Payment Intent
        PaymentIntentEntity paymentIntent = paymentIntentRepository
                .findByPaymentIntentReference(
                        idempotencyKey.getResourceReference()
                ).orElseThrow(() ->
                        new ResourceNotFoundException("Payment intent not found."));

        // Load the payment associated with this payment intent
        PaymentEntity payment =
                paymentService.findPaymentByPaymentIntent(paymentIntent);

        log.info("Returning existing payment intent. Payment Intent Reference: {}",
                paymentIntent.getPaymentIntentReference());

        return paymentMapper.toCreatePaymentIntentResponse(
                paymentIntent,
                payment,
                razorpayProperties.getKeyId()
        );
    }

    private void validateRequest(CreatePaymentIntentRequest request) {
        if (!"INR".equals(request.getCurrency())) {
            throw new BadRequestException("Currently only INR currency is supported.");
        }
        paymentIntentRepository.findByPaymentIntentReference(request.getOrderReference())
                .ifPresent(paymentIntent -> {
                    throw new ConflictException("A payment intent already exists for this order.");
                });
    }

    private PaymentIntentEntity createPaymentIntentEntity(CreatePaymentIntentRequest request) {
        PaymentIntentEntity entity = new PaymentIntentEntity();
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

    private GatewayOrderRequest buildGatewayOrderRequest(PaymentIntentEntity paymentIntent) {
        return GatewayOrderRequest.builder()
                .receipt(paymentIntent.getPaymentIntentReference())
                .amount(paymentIntent.getAmount())
                .currency(paymentIntent.getCurrency())
                .build();
    }
}
