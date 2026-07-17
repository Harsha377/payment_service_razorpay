package com.streamhub.payment.webhook.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.common.exception.ResourceNotFoundException;
import com.streamhub.payment.gateway.dto.request.GatewayWebhookVerificationRequest;
import com.streamhub.payment.gateway.dto.response.GatewayPaymentDetailsResponse;
import com.streamhub.payment.gateway.dto.response.GatewayRefundDetailsResponse;
import com.streamhub.payment.gateway.dto.response.GatewayWebhookEventResponse;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.payment.entity.PaymentEntity;
import com.streamhub.payment.payment.enums.PaymentStatus;
import com.streamhub.payment.payment.repository.PaymentRepository;
import com.streamhub.payment.payment.service.PaymentService;
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
            case "payment.captured" -> handlePaymentCaptured(root);
            case "payment.failed" -> handlePaymentFailed(root);
            case "payment.authorized" -> handlePaymentAuthorized(root);
            case "order.paid" -> handleOrderPaid(root);
            case "refund.processed" -> handleRefundProcessed(root);
            case "refund.failed" -> handleRefundFailed(root);
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

        log.info(
                "Payment authorized. Gateway Order Id: {}, Gateway Payment Id: {}",
                gatewayOrderId,
                gatewayPaymentId
        );

        log.info(
                "Ignoring payment.authorized because the application is configured for Auto Capture."
        );

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
    private void handleRefundProcessed(JsonNode root){
        log.info("Processing refund.processed webhook.");
        JsonNode refundNode = root
                .path("payload")
                .path("refund").path("entity");
        String gatewayRefundId = refundNode.path("id").asText();
        if (gatewayRefundId.isBlank()){
            throw new BadRequestException("Gateway refund id is missing.");
        }
        RefundEntity refund=refundRepository.findByGatewayRefundId(gatewayRefundId)
                .orElseThrow(()-> new
                        ResourceNotFoundException("Refund not found."));

        log.info("Refund found. Refund Reference: {}", refund.getRefundReference());

        if (refund.getStatus()== RefundStatus.SUCCESS){
            log.info("Ignoring duplicate refund.processed webhook. Refund Reference: {}",
                    refund.getRefundReference());
            return;
        }

        GatewayRefundDetailsResponse gatewayDetails =
                razorpayService.fetchRefund(gatewayRefundId);
        RefundEntity completedRefund =refundService.completeRefund(
                refund, gatewayDetails);

        log.info("Refund processed successfully. Refund Reference: {}",
                completedRefund.getRefundReference());


    }
    private void handleRefundFailed(JsonNode root){
        log.info("Processing refund.failed webhook.");
        JsonNode refundNode = root.path("payload")
                .path("refund")
                .path("entity");
        String gatewayRefundId = refundNode.path("id").asText();
        if (gatewayRefundId.isBlank()){
            throw new BadRequestException("Gateway refund id is missing.");
        }
        RefundEntity refund = refundRepository.findByGatewayRefundId(gatewayRefundId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refund not found."));
        log.info("Refund found. Refund Reference number: {}",refund.getRefundReference());
        if (refund.getStatus()==RefundStatus.FAILED){
            log.info("Ignoring duplicate refund.failed webhook. Refund Reference: {}",
                    refund.getRefundReference());
            return;
        }
        GatewayRefundDetailsResponse gatewayDetails =
                razorpayService.fetchRefund(gatewayRefundId);
        RefundEntity failedRefund= refundService
                .failRefund(refund,gatewayDetails);
        log.info("Refund failed. Refund Reference: {}", failedRefund.getRefundReference());

    }

    @Override
    public GatewayWebhookEventResponse parseWebhook(String payload) {
        return null;
    }


}
