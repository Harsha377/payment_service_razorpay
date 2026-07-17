package com.streamhub.payment.refund.service.impl;

import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ResourceNotFoundException;
import com.streamhub.payment.common.util.UlidGenerator;
import com.streamhub.payment.gateway.dto.request.GatewayRefundRequest;
import com.streamhub.payment.gateway.dto.response.GatewayRefundDetailsResponse;
import com.streamhub.payment.gateway.dto.response.GatewayRefundResponse;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.enums.PaymentStatus;
import com.streamhub.payment.payment.repository.PaymentRepository;
import com.streamhub.payment.refund.dto.request.CreateRefundRequest;
import com.streamhub.payment.refund.dto.response.CreateRefundResponse;
import com.streamhub.payment.refund.dto.response.RefundResponse;
import com.streamhub.payment.refund.dto.response.RefundSummaryResponse;
import com.streamhub.payment.refund.entity.RefundEntity;
import com.streamhub.payment.refund.enums.RefundEventSource;
import com.streamhub.payment.refund.enums.RefundEventType;
import com.streamhub.payment.refund.enums.RefundStatus;
import com.streamhub.payment.refund.mapper.RefundMapper;
import com.streamhub.payment.refund.repository.RefundRepository;
import com.streamhub.payment.refund.service.RefundEventService;
import com.streamhub.payment.refund.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayService razorpayService;
    private final RefundEventService refundEventService;
    private final RefundMapper refundMapper;


    @Override
    public CreateRefundResponse createRefund(CreateRefundRequest request) {
       PaymentEntity payment=findPayment(request.getPaymentReference());
       validatePayment(payment);
        Long remainingRefundableAmount =calculateRemainingRefundableAmount(payment);
        validateRefundAmount(request.getAmount(),remainingRefundableAmount);
        RefundEntity refund = buildRefund(payment, request);
        refund=refundRepository.save(refund);
        GatewayRefundResponse gatewayRefund = razorpayService.createRefund(
                GatewayRefundRequest.builder()
                .gatewayPaymentId(payment.getGatewayPaymentId())
                .amount(refund.getAmount())
                .speed(refund.getSpeed())
                .notes(null)
                .build() );
        refund.setGatewayRefundId(gatewayRefund.getGatewayRefundId());
        refund.setGatewayStatus(gatewayRefund.getGatewayStatus());
        refund.setStatus(RefundStatus.PROCESSING);
        refund=refundRepository.save(refund);
        refundEventService.recordEvent(refund,
                RefundEventType.REFUND_REQUESTED,
                RefundEventSource.API,null);
        return refundMapper.toCreateRefundResponse(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefund(String refundReference) {
        RefundEntity refund =  refundRepository.findByRefundReference(refundReference)
                .orElseThrow(()-> new ResourceNotFoundException("Refund not found"));
        return refundMapper.toRefundResponse(refund);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundSummaryResponse> getRefundsByPayment(String paymentReference) {
        PaymentEntity payment = findPayment(paymentReference);
        List<RefundEntity> refunds = refundRepository.findAllByPayment(payment);
        return refunds.stream().map(refundMapper::toRefundSummaryResponse).toList();
    }

    private PaymentEntity findPayment(String paymentReference){
        return paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(()->
                        new ResourceNotFoundException("Payment not found."));
    }

    private void validatePayment(PaymentEntity payment){
        if (payment.getStatus() != PaymentStatus.SUCCESS){
            throw new BadRequestException("Only successful payments can be refunded.");
        }
    }

    private Long calculateRemainingRefundableAmount(PaymentEntity payment){
        Long refundedAmount=refundRepository.sumSuccessfulRefundAmountByPayment(payment);
        return payment.getAmount()-refundedAmount;
    }

    private void validateRefundAmount(Long requestedAmount,Long remainingRefundableAmount){
        if (requestedAmount<=0){
            throw new BadRequestException("Refund amount must be greater than zero.");
        }

        if (requestedAmount>remainingRefundableAmount){
            throw new BadRequestException("Refund amount exceeds the remaining refundable amount.");
        }

    }

    private RefundEntity buildRefund(PaymentEntity payment,CreateRefundRequest request){
        RefundEntity refund=new RefundEntity();
        refund.setRefundReference(UlidGenerator.generate());
        refund.setPayment(payment);
        refund.setAmount(request.getAmount());
        refund.setCurrency(payment.getCurrency());
        refund.setReason(request.getReason());
        refund.setSpeed(request.getSpeed());
        refund.setInitiator(request.getInitiator());
        refund.setStatus(RefundStatus.REQUESTED);
        refund.setMetadata(request.getMetadata());

        return refund;

    }

    @Override
    public RefundEntity completeRefund(RefundEntity refund, GatewayRefundDetailsResponse gatewayDetails) {
        refund.setStatus(RefundStatus.SUCCESS);
        refund.setGatewayStatus(gatewayDetails.getGatewayStatus());
        refund.setGatewayResponse(gatewayDetails.getGatewayResponse());
        refund.setGatewayNotes(gatewayDetails.getGatewayNotes());
        refund.setProcessedAt(OffsetDateTime.now());
        RefundEntity completedRefund = refundRepository.save(refund);
        refundEventService.recordEvent(completedRefund,
                RefundEventType.REFUND_SUCCESS,
                RefundEventSource.WEBHOOK,
                gatewayDetails.getGatewayResponse() );

        log.info("Refund completed successfully. Refund Reference: {}",
                completedRefund.getRefundReference());
        return completedRefund;
    }

    @Override
    public RefundEntity failRefund(RefundEntity refund, GatewayRefundDetailsResponse gatewayDetails) {
        refund.setStatus(RefundStatus.FAILED);
        refund.setGatewayStatus(gatewayDetails.getGatewayStatus());
        refund.setGatewayResponse(gatewayDetails.getGatewayResponse());
        refund.setGatewayNotes(gatewayDetails.getGatewayNotes());
        RefundEntity failedRefund = refundRepository.save(refund);
        refundEventService.recordEvent(
                failedRefund,
                RefundEventType.REFUND_FAILED,
                RefundEventSource.WEBHOOK,
                gatewayDetails.getGatewayResponse()
        );
        log.info("Refund marked as failed. Refund Reference: {}",
                failedRefund.getRefundReference());
        return failedRefund;
    }
}
