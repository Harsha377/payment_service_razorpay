package com.streamhub.payment.gateway.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamhub.payment.common.exception.ApiException;
import com.streamhub.payment.gateway.dto.request.GatewayContactRequest;
import com.streamhub.payment.gateway.dto.request.GatewayFundAccountRequest;
import com.streamhub.payment.gateway.dto.response.GatewayContactResponse;
import com.streamhub.payment.gateway.dto.response.GatewayContactResult;
import com.streamhub.payment.gateway.dto.response.GatewayFundAccountResponse;
import com.streamhub.payment.gateway.dto.response.GatewayFundAccountResult;
import com.streamhub.payment.gateway.enums.RazorpayContactType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RazorpayXClientImpl implements RazorpayXClient {
    private final RestClient razorpayXRestClient;
    private final ObjectMapper objectMapper;

    @Override
    public GatewayContactResult createContact(GatewayContactRequest request) {
        log.info("Creating Razorpay contact. Email: {}, contact: {}", request.getEmail(), request.getContact());
        try {
            JsonNode gatewayResponse = razorpayXRestClient.post()
                    .uri("/contacts")
                    .body(buildContactRequest(request))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            (requestData, responseData) -> {

                                String errorBody = new String(responseData.getBody().readAllBytes());

                                log.error("Razorpay Contact API failed. Status: {}, Response: {}",
                                        responseData.getStatusCode(), errorBody);

                                throw new ApiException("Razorpay Contact API failed.",
                                        HttpStatus.BAD_GATEWAY);
                            })
                    .body(JsonNode.class);
            if (gatewayResponse == null) {
                throw new ApiException("Empty response received from Razorpay.", HttpStatus.BAD_GATEWAY);
            }
            GatewayContactResponse response =
                    objectMapper.treeToValue(
                            gatewayResponse,
                            GatewayContactResponse.class);
            log.info("Razorpay contact created successfully. Contact Id: {}",
                    response.getId());
            return GatewayContactResult.builder()
                    .response(response)
                    .gatewayResponse(gatewayResponse)
                    .build();

        } catch (RestClientException exception) {
            log.error("Failed to create Razorpay contact.", exception);
            throw new ApiException("Failed to create Razorpay contact. " +
                    exception.getMessage(), HttpStatus.BAD_GATEWAY);
        } catch (Exception exception) {

            log.error("Failed to parse Razorpay contact response.", exception);
            throw new ApiException("Failed to parse Razorpay response.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> buildContactRequest(
            GatewayContactRequest request) {

        Map<String, Object> body = new HashMap<>();

        body.put("name", request.getName());
        body.put("email", request.getEmail());
        body.put("contact", request.getContact());
        body.put("type", mapContactType(request.getType()));
        body.put("notes", request.getNotes());

        return body;
    }

    private String mapContactType(RazorpayContactType contactType) {

        return switch (contactType) {

            case CUSTOMER -> "customer";
            case EMPLOYEE -> "employee";
            case VENDOR -> "vendor";
            case SELF -> "self";
        };
    }

    @Override
    public GatewayFundAccountResult createFundAccount(
            GatewayFundAccountRequest request) {

        log.info(
                "Creating Razorpay fund account for Contact Id: {}",
                request.getContactId());

        try {

            JsonNode gatewayResponse = razorpayXRestClient
                    .post()
                    .uri("/fund_accounts")
                    .body(buildFundAccountRequest(request))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (requestData, responseData) -> {

                                String errorBody =
                                        new String(responseData.getBody().readAllBytes());

                                log.error(
                                        "Razorpay Fund Account API failed. Status: {}, Response: {}",
                                        responseData.getStatusCode(),
                                        errorBody);

                                throw new ApiException(
                                        "Razorpay Fund Account API failed.",
                                        HttpStatus.BAD_GATEWAY);})
                    .body(JsonNode.class);

            if (gatewayResponse == null) {
                throw new ApiException(
                        "Empty response received from Razorpay.", HttpStatus.BAD_GATEWAY);
            }

            GatewayFundAccountResponse response =
                    objectMapper.treeToValue(
                            gatewayResponse, GatewayFundAccountResponse.class);

            log.info("Razorpay Fund Account created successfully. Fund Account Id: {}",
                    response.getId());

            return GatewayFundAccountResult.builder()
                    .response(response)
                    .gatewayResponse(gatewayResponse).build();

        } catch (RestClientException exception) {
            log.error("Failed to create Razorpay Fund Account.",
                    exception);

            throw new ApiException("Failed to create Razorpay Fund Account.",
                    HttpStatus.BAD_GATEWAY);

        } catch (Exception exception) {

            log.error("Failed to parse Razorpay Fund Account response.", exception);

            throw new ApiException("Failed to parse Razorpay response.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> buildFundAccountRequest(
            GatewayFundAccountRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("contact_id", request.getContactId());
        body.put("account_type", request.getAccountType());

        if (request.getBankAccount() != null) {

            Map<String, Object> bankAccount = new HashMap<>();

            bankAccount.put("name", request.getBankAccount().getName());
            bankAccount.put("ifsc", request.getBankAccount().getIfsc());
            bankAccount.put("account_number",
                    request.getBankAccount().getAccountNumber());

            body.put("bank_account", bankAccount);
        }

        if (request.getVpa() != null) {

            Map<String, Object> vpa = new HashMap<>();
            vpa.put("address", request.getVpa().getAddress());
            body.put("vpa", vpa);
        }

        return body;
    }


}
