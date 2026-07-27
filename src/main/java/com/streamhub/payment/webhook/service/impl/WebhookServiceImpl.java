package com.streamhub.payment.webhook.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ConflictException;
import com.streamhub.payment.common.exception.ResourceNotFoundException;
import com.streamhub.payment.gateway.dto.request.GatewayWebhookVerificationRequest;
import com.streamhub.payment.gateway.dto.response.GatewayPaymentDetailsResponse;
import com.streamhub.payment.gateway.dto.response.GatewayRefundDetailsResponse;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.enums.PaymentStatus;
import com.streamhub.payment.payment.repository.PaymentRepository;
import com.streamhub.payment.payment.service.PaymentService;
import com.streamhub.payment.payout.entity.PayoutEntity;
import com.streamhub.payment.payout.enums.PayoutEventType;
import com.streamhub.payment.payout.enums.PayoutStatus;
import com.streamhub.payment.payout.repository.PayoutRepository;
import com.streamhub.payment.payout.service.PayoutService;
import com.streamhub.payment.refund.entity.RefundEntity;
import com.streamhub.payment.refund.enums.RefundStatus;
import com.streamhub.payment.refund.repository.RefundRepository;
import com.streamhub.payment.refund.service.RefundService;
import com.streamhub.payment.webhook.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebhookServiceImpl implements WebhookService {
    private final RazorpayService razorpayService;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final RefundRepository refundRepository;
    private final RefundService refundService;
    private final PayoutRepository payoutRepository;
    private final PayoutService payoutService;

    @Override
    public void processWebhook(
            GatewayWebhookVerificationRequest request
    ) {

        log.info("Received Razorpay webhook.");

        // Step 1: Verify signature
        razorpayService.verifyWebhookSignature(request);

        // Parse JSON
        JsonNode root = parsePayload(request.getPayload());
        String event = root.path("event").asText();

        log.info("Webhook Event : {}", event);


        routeEvent(event, root);

    }

    private void routeEvent(String event, JsonNode root) {
        switch (event) {
            // -------- Payment --------
            case "payment.captured" -> handlePaymentCaptured(root);
            case "payment.failed" -> handlePaymentFailed(root);
            case "payment.authorized" -> handlePaymentAuthorized(root);
            case "order.paid" -> handleOrderPaid(root);
            // -------- Refund --------
            case "refund.processed" -> handleRefundProcessed(root);
            case "refund.failed" -> handleRefundFailed(root);
            // -------- Payout --------
            case "payout.processing" -> handlePayoutProcessing(root);
            case "payout.processed" -> handlePayoutProcessed(root);
            case "payout.failed" -> handlePayoutFailed(root);
            case "payout.reversed" -> handlePayoutReversed(root);
            default -> log.info("Ignoring unsupported webhook event : {}", event);
        }
    }

    private void handlePaymentCaptured(JsonNode root) {
        log.info("processing payment.captured");
        JsonNode paymentNode = root.path("payload").path("payment").path("entity");
        String gatewayPaymentId = paymentNode.path("id").asText();
        String gatewayOrderId = paymentNode.path("order_id").asText();
        if (gatewayPaymentId.isBlank()) {
            throw new BadRequestException("Gateway payment id is missing.");
        }

        if (gatewayOrderId.isBlank()) {
            throw new BadRequestException("Gateway order id is missing.");
        }
        PaymentEntity payment = paymentRepository.findByGatewayOrderId(gatewayOrderId).orElseThrow(() ->
                new ResourceNotFoundException("Payment not found."));

        log.info("Payment found. Payment Reference: {}", payment.getPaymentReference());
        if (payment.getStatus() == PaymentStatus.SUCCESS) {

            log.info("Ignoring duplicate payment.captured webhook. Payment Reference: {}",
                    payment.getPaymentReference());
            return;
        }
        PaymentEntity completedPayment = paymentService.completePayment(payment, gatewayPaymentId, null);
        log.info("Webhook payment processed successfully. Payment Reference: {}",
                completedPayment.getPaymentReference());

    }

    private void handlePaymentFailed(JsonNode root) {
        log.info("processing payment.failed");
        JsonNode paymentNode = root.path("payload").path("payment").path("entity");
        String gatewayPaymentId = paymentNode.path("id").asText();
        String gatewayOrderId = paymentNode.path("order_id").asText();
        if (gatewayPaymentId.isBlank()) {
            throw new BadRequestException(
                    "Gateway payment id is missing."
            );
        }

        if (gatewayOrderId.isBlank()) {
            throw new BadRequestException("Gateway order id is missing.");
        }
        PaymentEntity payment = paymentRepository.findByGatewayOrderId(gatewayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found."));
        GatewayPaymentDetailsResponse gatewayDetails = razorpayService.fetchPayment(gatewayPaymentId);
        if (payment.getStatus() == PaymentStatus.FAILED) {
            log.info("Ignoring duplicate payment.failed webhook. Payment Reference: {}", payment.getPaymentReference());
            return;
        }
        PaymentEntity failedPayment = paymentService.failPayment(payment, gatewayPaymentId, gatewayDetails);
        log.info("Webhook processed successfully. Payment Reference: {}",
                failedPayment.getPaymentReference());
    }

    private JsonNode parsePayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid webhook payload.");
        }

    }

    private void handlePaymentAuthorized(JsonNode root) {

        log.info("Processing payment.authorized webhook.");

        JsonNode paymentNode = root
                .path("payload")
                .path("payment")
                .path("entity");

        String gatewayPaymentId = paymentNode.path("id").asText();
        String gatewayOrderId = paymentNode.path("order_id").asText();

        if (gatewayPaymentId.isBlank()) {
            throw new BadRequestException("Gateway payment id is missing.");
        }

        if (gatewayOrderId.isBlank()) {
            throw new BadRequestException("Gateway order id is missing.");
        }

        log.info("Payment authorized. Gateway Order Id: {}, Gateway Payment Id: {}",
                gatewayOrderId, gatewayPaymentId);

        log.info("Ignoring payment.authorized because the application is configured for Auto Capture.");

    }

    private void handleOrderPaid(JsonNode root) {

        log.info("Processing order.paid webhook.");
        JsonNode orderNode = root
                .path("payload")
                .path("order")
                .path("entity");

        String gatewayOrderId = orderNode.path("id").asText();
        if (gatewayOrderId.isBlank()) {
            throw new BadRequestException("Gateway order id is missing.");
        }

        log.info("Order paid successfully. Gateway Order Id: {}", gatewayOrderId);
        log.info("Ignoring order.paid because payment.captured already updates the payment state.");

    }

    private void handleRefundProcessed(JsonNode root) {
        log.info("Processing refund.processed webhook.");
        JsonNode refundNode = root
                .path("payload")
                .path("refund").path("entity");
        String gatewayRefundId = refundNode.path("id").asText();
        if (gatewayRefundId.isBlank()) {
            throw new BadRequestException("Gateway refund id is missing.");
        }
        RefundEntity refund = refundRepository.findByGatewayRefundId(gatewayRefundId)
                .orElseThrow(() -> new
                        ResourceNotFoundException("Refund not found."));

        log.info("Refund found. Refund Reference: {}", refund.getRefundReference());

        if (refund.getStatus() == RefundStatus.SUCCESS) {
            log.info("Ignoring duplicate refund.processed webhook. Refund Reference: {}",
                    refund.getRefundReference());
            return;
        }

        GatewayRefundDetailsResponse gatewayDetails =
                razorpayService.fetchRefund(gatewayRefundId);
        RefundEntity completedRefund = refundService.completeRefund(
                refund, gatewayDetails);

        log.info("Refund processed successfully. Refund Reference: {}",
                completedRefund.getRefundReference());


    }

    private void handleRefundFailed(JsonNode root) {
        log.info("Processing refund.failed webhook.");
        JsonNode refundNode = root.path("payload")
                .path("refund")
                .path("entity");
        String gatewayRefundId = refundNode.path("id").asText();
        if (gatewayRefundId.isBlank()) {
            throw new BadRequestException("Gateway refund id is missing.");
        }
        RefundEntity refund = refundRepository.findByGatewayRefundId(gatewayRefundId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refund not found."));
        log.info("Refund found. Refund Reference number: {}", refund.getRefundReference());
        if (refund.getStatus() == RefundStatus.FAILED) {
            log.info("Ignoring duplicate refund.failed webhook. Refund Reference: {}",
                    refund.getRefundReference());
            return;
        }
        GatewayRefundDetailsResponse gatewayDetails =
                razorpayService.fetchRefund(gatewayRefundId);
        RefundEntity failedRefund = refundService
                .failRefund(refund, gatewayDetails);
        log.info("Refund failed. Refund Reference: {}", failedRefund.getRefundReference());

    }

    // -------- Payout --------

    private void handlePayoutProcessing(JsonNode root) {

        log.info("Processing payout.processing webhook.");

        JsonNode payoutNode = root
                .path("payload")
                .path("payout")
                .path("entity");

        String gatewayPayoutId = payoutNode.path("id").asText();

        if (gatewayPayoutId.isBlank()) {
            throw new BadRequestException("Gateway payout id is missing.");
        }

        PayoutEntity payout = payoutRepository
                .findByGatewayPayoutId(gatewayPayoutId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payout not found for gateway payout id: " + gatewayPayoutId));

        if (isDuplicatePayoutWebhook(
                payout, PayoutStatus.PROCESSING)) {
            return;
        }

        validatePayoutStatusTransition(
                payout.getStatus(), PayoutStatus.PROCESSING);

        payoutService.updatePayoutFromWebhook(
                payout,
                PayoutStatus.PROCESSING,
                PayoutEventType.PAYOUT_PROCESSING,
                root,
                payoutNode);

        log.info(
                "Successfully processed payout.processing webhook. Payout Reference: {}",
                payout.getPayoutReference());
    }

    private void handlePayoutProcessed(JsonNode root) {
        log.info("Processing payout.processed webhook.");
        JsonNode payoutNode = root
                .path("payload")
                .path("payout")
                .path("entity");

        String gatewayPayoutId = payoutNode
                .path("id")
                .asText();
        if (gatewayPayoutId.isBlank()) {
            throw new BadRequestException("Gateway payout id is missing.");
        }

        PayoutEntity payout = payoutRepository.findByGatewayPayoutId(gatewayPayoutId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payout not found for gateway payout id: " + gatewayPayoutId));

        log.info("Payout found. Payout Reference: {}, Current Status: {}",
                payout.getPayoutReference(), payout.getStatus());

        // Idempotency - Ignore duplicate webhook deliveries
        if (isDuplicatePayoutWebhook(
                payout, PayoutStatus.PROCESSED)) {
            return;
        }

        validatePayoutStatusTransition(
                payout.getStatus(),
                PayoutStatus.PROCESSED);

        payoutService.updatePayoutFromWebhook(
                payout,
                PayoutStatus.PROCESSED,
                PayoutEventType.PAYOUT_PROCESSED,
                root,
                payoutNode);

    }

    private void handlePayoutFailed(JsonNode root) {

        log.info("Processing payout.failed webhook.");

        JsonNode payoutNode = root
                .path("payload")
                .path("payout")
                .path("entity");

        String gatewayPayoutId = payoutNode.path("id").asText();

        if (gatewayPayoutId.isBlank()) {
            throw new BadRequestException("Gateway payout id is missing.");
        }

        PayoutEntity payout = payoutRepository
                .findByGatewayPayoutId(gatewayPayoutId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payout not found for gateway payout id: "
                                        + gatewayPayoutId));

        if (isDuplicatePayoutWebhook(
                payout, PayoutStatus.FAILED)) {
            return;
        }

        validatePayoutStatusTransition(
                payout.getStatus(),
                PayoutStatus.FAILED);

        payoutService.updatePayoutFromWebhook(
                payout,
                PayoutStatus.FAILED,
                PayoutEventType.PAYOUT_FAILED,
                root,
                payoutNode);

        log.info("Successfully processed payout.failed webhook. Payout Reference: {}",
                payout.getPayoutReference());
    }

    private void handlePayoutReversed(JsonNode root) {

        log.info("Processing payout.reversed webhook.");

        JsonNode payoutNode = root
                .path("payload")
                .path("payout")
                .path("entity");

        String gatewayPayoutId = payoutNode.path("id").asText();

        if (gatewayPayoutId.isBlank()) {
            throw new BadRequestException("Gateway payout id is missing.");
        }

        PayoutEntity payout = payoutRepository
                .findByGatewayPayoutId(gatewayPayoutId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payout not found for gateway payout id: " + gatewayPayoutId));

        if (isDuplicatePayoutWebhook(
                payout, PayoutStatus.REVERSED)) {
            return;
        }

        validatePayoutStatusTransition(
                payout.getStatus(), PayoutStatus.REVERSED);

        payoutService.updatePayoutFromWebhook(
                payout,
                PayoutStatus.REVERSED,
                PayoutEventType.PAYOUT_REVERSED,
                root,
                payoutNode);

        log.info("Successfully processed payout.reversed webhook. Payout Reference: {}",
                payout.getPayoutReference());
    }

    private boolean isDuplicatePayoutWebhook(PayoutEntity payout,PayoutStatus expectedStatus){
        if (payout.getStatus()==expectedStatus){
            log.info("Ignoring duplicate payout webhook. Payout Reference: {}, Status: {}",payout.getPayoutReference(),expectedStatus);
            return true;
        }
        return false;
    }

    private void validatePayoutStatusTransition(PayoutStatus currentStatus,PayoutStatus newStatus){
        // Duplicate webhook is handled separately
        if (currentStatus==newStatus){
            return;
        }

        switch (currentStatus){
            case CREATED -> {
                // CREATED can move to PENDING, PROCESSING, FAILED, PROCESSED
            }
            case PENDING -> {
                if (newStatus != PayoutStatus.PROCESSING
                        && newStatus != PayoutStatus.PROCESSED
                        && newStatus != PayoutStatus.FAILED
                        && newStatus != PayoutStatus.REVERSED){
                    throw new ConflictException("Invalid payout status transition from PENDING to "+newStatus);
                }
            }

            case PROCESSING -> {
                if (newStatus != PayoutStatus.PROCESSED
                        && newStatus != PayoutStatus.FAILED
                        && newStatus != PayoutStatus.REVERSED ){
                    throw new ConflictException("Invalid payout status transition from PROCESSING to "+newStatus);
                }
            }

            case PROCESSED ->
                    throw new ConflictException("Payout is already processed. Status cannot change");

            case FAILED ->
                    throw new ConflictException("Failed payout cannot transition to " + newStatus);

            case REVERSED ->
                    throw new ConflictException("Reversed payout cannot transition to " + newStatus);

            case CANCELLED ->
                    throw new ConflictException("Cancelled payout cannot transition to " + newStatus);
        }
    }


}
