package com.streamhub.payment.payment.service.impl;

import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ConflictException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

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

        // 1. Find Payment
        PaymentEntity payment = paymentRepository
                .findByGatewayOrderId(request.getGatewayOrderId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Payment not found."));

        // 2. Prevent Duplicate Verification
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new ConflictException("Payment is already verified.");
        }

        // 3. Validate Current Payment State
        if (payment.getStatus() != PaymentStatus.CREATED) {
            throw new BadRequestException("Payment cannot be verified in the current state.");
        }

        // 4. Build Gateway Verification Request
        GatewayPaymentVerificationRequest gatewayRequest =
                GatewayPaymentVerificationRequest.builder()
                        .gatewayOrderId(request.getGatewayOrderId())
                        .gatewayPaymentId(request.getGatewayPaymentId())
                        .gatewaySignature(request.getGatewaySignature())
                        .build();

        // 5. Verify Razorpay Signature
        razorpayService.verifyPayment(gatewayRequest);
        GatewayPaymentDetailsResponse gatewayDetails = razorpayService.fetchPayment(request.getGatewayPaymentId());

        // 6. Update Payment
        OffsetDateTime now = OffsetDateTime.now();

        payment.setGatewayPaymentId(request.getGatewayPaymentId());
        payment.setGatewaySignature(request.getGatewaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(now);
        payment.setPaymentMethod(gatewayDetails.getPaymentMethod());
        payment.setGatewayStatus(gatewayDetails.getGatewayStatus());
        payment.setGatewayResponse(gatewayDetails.getGatewayResponse());

        // 7. Update Payment Intent
        PaymentIntentEntity paymentIntent = payment.getPaymentIntent();
        paymentIntent.setStatus(PaymentIntentStatus.COMPLETED);

        // 8. Record Payment Event
        paymentEventService.recordPaymentSuccess(payment);

        // 9. Build Response
        return VerifyPaymentResponse.builder()
                .paymentReference(payment.getPaymentReference())
                .paymentIntentReference(paymentIntent.getPaymentIntentReference())
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .status(payment.getStatus())
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
}
