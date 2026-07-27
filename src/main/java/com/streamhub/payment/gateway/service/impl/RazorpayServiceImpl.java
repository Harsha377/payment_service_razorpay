package com.streamhub.payment.gateway.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.*;
import com.streamhub.payment.common.exception.ApiException;
import com.streamhub.payment.common.exception.BadRequestException;
import com.streamhub.payment.gateway.client.RazorpayXClient;
import com.streamhub.payment.gateway.config.properties.RazorpayProperties;
import com.streamhub.payment.gateway.dto.request.*;
import com.streamhub.payment.gateway.dto.response.*;
import com.streamhub.payment.gateway.service.RazorpayService;
import com.streamhub.payment.gateway.util.RazorpayPaymentMethodMapper;
import com.streamhub.payment.payment.enums.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayServiceImpl implements RazorpayService {

    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final ObjectMapper objectMapper;
    private final RazorpayXClient razorpayXClient;

    @Override
    public GatewayOrderResponse createOrder(GatewayOrderRequest request) {
        try {
            JSONObject orderRequest = new JSONObject();

            orderRequest.put("amount", request.getAmount());
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", request.getReceipt());
            Order order = razorpayClient.orders.create(orderRequest);
            return GatewayOrderResponse.builder()
                    .gatewayOrderId(order.get("id"))
                    .gatewayName(PaymentGateway.RAZORPAY.name())
                    .status(order.get("status"))
                    .keyId(razorpayProperties.getKeyId())
                    .build();
        } catch (RazorpayException ex) {
            throw new ApiException(
                    "Failed to create Razorpay order.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @Override
    public void verifyPayment(GatewayPaymentVerificationRequest request) {

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.getGatewayOrderId());
            attributes.put("razorpay_payment_id", request.getGatewayPaymentId());
            attributes.put("razorpay_signature", request.getGatewaySignature());

            Utils.verifyPaymentSignature(attributes, razorpayProperties.getKeySecret());

        } catch (RazorpayException ex) {
            throw new BadRequestException("Invalid payment signature.");
        }
    }

    @Override
    public GatewayPaymentDetailsResponse fetchPayment(String gatewayPaymentId) {
        try {
            Payment payment = razorpayClient.payments.fetch(gatewayPaymentId);
            JsonNode gatewayResponse = objectMapper.readTree(payment.toString());
            return GatewayPaymentDetailsResponse.builder()
                    .paymentMethod(RazorpayPaymentMethodMapper.map(payment.get("method")))
                    .gatewayStatus(payment.get("status"))
                    .gatewayResponse(gatewayResponse).build();
        } catch (Exception ex) {

            throw new ApiException(
                    "Failed to fetch payment details from Razorpay.",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );

        }

    }

    @Override
    public void verifyWebhookSignature(GatewayWebhookVerificationRequest request) {
        try {
            boolean verified = Utils.verifyWebhookSignature(
                    request.getPayload(),
                    request.getSignature(),
                    razorpayProperties.getWebhookSecret());

            if (!verified) {
                throw new BadRequestException("Invalid webhook signature.");
            }

            log.info("Webhook signature verified successfully.");

        } catch (RazorpayException ex) {
            throw new BadRequestException("Unable to verify webhook signature.");

        }

    }

    @Override
    public GatewayRefundResponse createRefund(GatewayRefundRequest request) {
       try{
           JSONObject refundRequest=buildRefundRequest(request);
          Refund refund=razorpayClient.payments.refund(request.getGatewayPaymentId(),refundRequest);
          return mapRefundResponse(refund);
       }
       catch (RazorpayException ex){
           log.error("Failed to create Razorpay refund. Gateway Payment Id: {}",
                   request.getGatewayPaymentId(), ex);
           throw new ApiException( "Failed to create Razorpay refund.",HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @Override
    public GatewayRefundDetailsResponse fetchRefund(String gatewayRefundId) {
       try{
           Refund refund=razorpayClient.refunds.fetch(gatewayRefundId);
           return mapRefundDetailsResponse(refund);
       }
       catch (RazorpayException ex){
           log.error("Failed to fetch Razorpay refund. Gateway Refund Id: {}",gatewayRefundId,ex);
           throw new ApiException("Failed to fetch refund details from Razorpay.",HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    private JSONObject buildRefundRequest(GatewayRefundRequest request){
        JSONObject refundRequest=new JSONObject();
        refundRequest.put("amount",request.getAmount());
        refundRequest.put("speed",request.getSpeed().name().toLowerCase());
        if (request.getNotes()!=null){
            refundRequest.put("notes",new JSONObject(request.getNotes().toString()));
        }
        return refundRequest;
    }

    private GatewayRefundResponse mapRefundResponse(Refund refund){
        return GatewayRefundResponse.builder()
                .gatewayRefundId(refund.get("id"))
                .gatewayStatus(refund.get("status"))
                .build();
    }
    private GatewayRefundDetailsResponse mapRefundDetailsResponse(Refund refund){
       try{
           JsonNode gatewayResponse=objectMapper.readTree(refund.toString());
           JsonNode gatewayNotes = gatewayResponse.path("notes");
           return GatewayRefundDetailsResponse.builder()
                   .gatewayRefundId(refund.get("id"))
                   .gatewayStatus(refund.get("status"))
                   .gatewayResponse(gatewayResponse)
                   .gatewayNotes(gatewayNotes.isMissingNode()?null:gatewayNotes)
                   .build();
       }
       catch (Exception ex){
          throw new ApiException( "Failed to map Razorpay refund response.",
                  HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }

    @Override
    public GatewayContactResult  createContact(GatewayContactRequest request) {
        return razorpayXClient.createContact(request);
    }

    @Override
    public GatewayFundAccountResult createFundAccount(GatewayFundAccountRequest request) {
        return razorpayXClient.createFundAccount(request);
    }

    @Override
    public GatewayPayoutResult createPayout(GatewayPayoutRequest request) {
        return razorpayXClient.createPayout(request);
    }

    @Override
    public GatewayPayoutDetailsResponse fetchPayout(String gatewayPayoutId) {
        return razorpayXClient.fetchPayout(gatewayPayoutId);
    }


}

/*
RazorpayService

├── createOrder()

├── verifyPayment()

├── fetchPayment()

├── verifyWebhookSignature()

├── createRefund()

└── fetchRefund()
 */
