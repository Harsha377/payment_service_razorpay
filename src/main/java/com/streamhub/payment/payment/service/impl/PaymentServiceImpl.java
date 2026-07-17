package com.streamhub.payment.payment.service.impl;

import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ResourceNotFoundException;
import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.gateway.dto.request.GatewayPaymentVerificationRequest;
import com.streamhub.payment.gateway.dto.response.GatewayOrderResponse;
import com.streamhub.payment.gateway.dto.response.GatewayPaymentDetailsResponse;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.payment.dto.request.VerifyPaymentRequest;
import com.streamhub.payment.payment.dto.response.PaymentResponse;
import com.streamhub.payment.payment.dto.response.VerifyPaymentResponse;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.entity.PaymentIntentEntity;
import com.streamhub.payment.payment.enums.PaymentGateway;
import com.streamhub.payment.payment.enums.PaymentIntentStatus;
import com.streamhub.payment.payment.enums.PaymentStatus;
import com.streamhub.payment.payment.repository.PaymentIntentRepository;
import com.streamhub.payment.payment.repository.PaymentRepository;
import com.streamhub.payment.payment.service.PaymentEventService;
import com.streamhub.payment.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final PaymentIntentRepository paymentIntentRepository;
    private final PaymentEventService paymentEventService;

    @Override
    @Transactional
    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) {

        log.info("Verifying payment. Gateway Order Id: {}",
                request.getGatewayOrderId());
        // Find Payment
        PaymentEntity payment = paymentRepository
                .findByGatewayOrderId(request.getGatewayOrderId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found."));

        // Prevent Duplicate Verification
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            log.info("Payment already verified. Payment Reference: {}",
                    payment.getPaymentReference());

            return VerifyPaymentResponse.builder()
                    .paymentReference(payment.getPaymentReference())
                    .paymentIntentReference(payment.getPaymentIntent().getPaymentIntentReference())
                    .gatewayPaymentId(payment.getGatewayPaymentId())
                    .status(payment.getStatus())
                    .message("Payment already verified.")
                    .build();
        }

        // Validate Current Payment State
        if (payment.getStatus() != PaymentStatus.CREATED) {
            log.warn("Invalid payment state: {}", payment.getStatus());
            throw new BadRequestException("Payment cannot be verified in the current state.");
        }

        //  Build Gateway Verification Request
        GatewayPaymentVerificationRequest gatewayRequest =
                GatewayPaymentVerificationRequest.builder()
                        .gatewayOrderId(request.getGatewayOrderId())
                        .gatewayPaymentId(request.getGatewayPaymentId())
                        .gatewaySignature(request.getGatewaySignature())
                        .build();

        //  Verify Razorpay Signature
        razorpayService.verifyPayment(gatewayRequest);
        log.info("Checkout signature verified successfully. Gateway Payment Id: {}",
                request.getGatewayPaymentId());
        // Complete Payment
        PaymentEntity completedPayment =completePayment(payment, request.getGatewayPaymentId(),
                request.getGatewaySignature());

        log.info("Payment verified successfully. Payment Reference: {}", payment.getPaymentReference());
        return VerifyPaymentResponse.builder()
                .paymentReference(completedPayment.getPaymentReference())
                .paymentIntentReference(completedPayment.getPaymentIntent().getPaymentIntentReference())
                .gatewayPaymentId(completedPayment.getGatewayPaymentId())
                .status(completedPayment.getStatus())
                .message("Payment verified successfully.")
                .build();
    }

    @Override
    public PaymentResponse getPayment(String paymentReference) {
        return null;
    }

    @Override
    public PaymentEntity createPayment(PaymentIntentEntity paymentIntent, GatewayOrderResponse gatewayOrder) {
       PaymentEntity payment =new PaymentEntity();
        payment .setPaymentReference(UlidGenerator.generate());
        payment .setPaymentIntent(paymentIntent);
        payment.setGatewayName(PaymentGateway.valueOf(gatewayOrder.getGatewayName()));
        payment.setGatewayOrderId(gatewayOrder.getGatewayOrderId());
        payment.setAmount(paymentIntent.getAmount());
        payment.setCurrency(paymentIntent.getCurrency());
        payment.setStatus(PaymentStatus.CREATED);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public PaymentEntity  completePayment(PaymentEntity payment, String gatewayPaymentId, String gatewaySignature) {
        log.info(  "Completing payment. Payment Reference: {}, Gateway Order Id: {}",
                payment.getPaymentReference(), payment.getGatewayOrderId());
        // Fetch latest payment details from Razorpay
        GatewayPaymentDetailsResponse  gatewayDetails =razorpayService.fetchPayment(gatewayPaymentId);
        OffsetDateTime now=OffsetDateTime.now();
        //Update Payment
        payment.setGatewayPaymentId(gatewayPaymentId);
        payment.setGatewaySignature(gatewaySignature);
        payment.setGatewayStatus(gatewayDetails.getGatewayStatus());
        payment.setGatewayResponse(gatewayDetails.getGatewayResponse());
        payment.setPaymentMethod(gatewayDetails.getPaymentMethod());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(now);

        paymentRepository.save(payment);
        log.info("Payment updated successfully. Payment Reference: {}",
                payment.getPaymentReference());

        // Update Payment Intent
        PaymentIntentEntity paymentIntent = payment.getPaymentIntent();
        paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);
        paymentIntentRepository.save(paymentIntent);

        log.info("Payment Intent completed. Payment Intent Reference: {}",
                paymentIntent.getPaymentIntentReference());
        // Record Event
        paymentEventService.recordPaymentSuccess(payment);
        log.info("Payment success event recorded. Payment Reference: {}",
                payment.getPaymentReference());

        return payment;
    }

    @Override
    public PaymentEntity failPayment(PaymentEntity payment,String gatewayPaymentId, GatewayPaymentDetailsResponse gatewayDetails) {
        log.info("Processing failed payment. Payment Reference: {}, Gateway Order Id: {}",
                payment.getPaymentReference(), payment.getGatewayOrderId());

        // Ignore duplicate failure
        if (payment.getStatus()==PaymentStatus.FAILED){
            log.info("Payment already marked as FAILED. Payment Reference: {}",
                    payment.getPaymentReference());
            return payment;
        }

        // Never downgrade a successful payment
        if (payment.getStatus()==PaymentStatus.SUCCESS){
            log.warn("Ignoring payment.failed webhook for successful payment. Payment Reference: {}",
                    payment.getPaymentReference());

            return payment;
        }
        // Update Payment
        payment.setGatewayPaymentId(gatewayPaymentId);
        payment.setGatewayStatus(gatewayDetails.getGatewayStatus());
        payment.setPaymentMethod(gatewayDetails.getPaymentMethod());
        payment.setGatewayResponse(gatewayDetails.getGatewayResponse());
        payment.setStatus(PaymentStatus.FAILED);
        payment.setPaidAt(null);
        paymentRepository.save(payment);
        log.info("Payment marked as FAILED. Payment Reference: {}", payment.getPaymentReference());
        // Record Event
        paymentEventService.recordPaymentFailed(payment);
        log.info("Payment failure event recorded. Payment Reference: {}",
                payment.getPaymentReference());
        return payment;

    }




}
