package com.streamhub.payment.gateway.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamhub.payment.common.exception.ApiException;
import com.streamhub.payment.gateway.dto.request.GatewayContactRequest;
import com.streamhub.payment.gateway.dto.request.GatewayFundAccountRequest;
import com.streamhub.payment.gateway.dto.request.GatewayPayoutRequest;
import com.streamhub.payment.gateway.dto.response.*;
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

                                throw buildGatewayException(errorBody, HttpStatus.BAD_GATEWAY);
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

                                throw buildGatewayException(errorBody, HttpStatus.BAD_GATEWAY);
                            })
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

    @Override
    public GatewayPayoutResult createPayout(GatewayPayoutRequest request) {
        log.info("Creating Razorpay payout. Fund Account Id: {}, Amount: {}",
                request.getFundAccountId(), request.getAmount());

        try {
            JsonNode gatewayResponse = razorpayXRestClient.post()
                    .uri("/payouts")
                    .body(buildPayoutRequest(request))
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (requestData, responseData) -> {

                                String errorBody =
                                        new String(responseData.getBody().readAllBytes());

                                log.error("Razorpay Payout API failed. Status: {}, Response: {}",
                                        responseData.getStatusCode(), errorBody);

                                throw buildGatewayException(errorBody, HttpStatus.BAD_GATEWAY);
                            })
                    .body(JsonNode.class);

            if (gatewayResponse == null) {
                throw new ApiException("Empty response received from Razorpay.",
                        HttpStatus.BAD_GATEWAY);
            }
            GatewayPayoutResponse response =
                    objectMapper.treeToValue(gatewayResponse, GatewayPayoutResponse.class);

            log.info("Razorpay payout created successfully. Payout Id: {}",
                    response.getId());

            return GatewayPayoutResult.builder()
                    .response(response)
                    .gatewayResponse(gatewayResponse)
                    .build();
        }
        catch (ApiException exception) {

            throw exception;

        }
        catch (RestClientException exception) {
            log.error("Failed to create Razorpay payout.", exception);
            throw new ApiException(exception.getMessage(), HttpStatus.BAD_GATEWAY);
        }
        catch (Exception exception) {

            log.error("Failed to parse Razorpay payout response.", exception);

            throw new ApiException("Failed to parse Razorpay response.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Map<String, Object> buildPayoutRequest(GatewayPayoutRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("account_number", request.getAccountNumber());
        body.put("fund_account_id", request.getFundAccountId());
        body.put("amount", request.getAmount());
        body.put("currency", request.getCurrency());
        body.put("mode", request.getMode());
        body.put("purpose", request.getPurpose());
        body.put("queue_if_low_balance", request.getQueueIfLowBalance());
        body.put("reference_id", request.getReferenceId());
        if (request.getNotes() != null) {
            body.put("notes", request.getNotes());
        }
        return body;
    }

    private ApiException buildGatewayException(
            String errorBody,
            HttpStatus status) throws JsonProcessingException {

        JsonNode errorNode = objectMapper.readTree(errorBody);

        String message = errorNode.path("error")
                .path("description")
                .asText("Gateway Error");

        String field = errorNode.path("error")
                .path("field")
                .asText();

        Map<String, String> errors = new HashMap<>();

        if (!field.isBlank()) {
            errors.put(field, message);
        }

        return new ApiException(message, status, errors);
    }

    @Override
    public GatewayPayoutDetailsResponse fetchPayout(String gatewayPayoutId) {
        log.info("Fetching Razorpay payout. Gateway Payout Id: {}", gatewayPayoutId);
        try{
            GatewayPayoutDetailsResponse response = razorpayXRestClient.get()
                    .uri("/payouts/{id}", gatewayPayoutId)
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            (requestData, responseData) -> {
                                String errorBody = new String(responseData.getBody().readAllBytes());
                                log.error("Failed to fetch Razorpay payout. Status: {}, Response: {}",
                                        responseData.getStatusCode(), errorBody);
                                throw buildGatewayException(errorBody, HttpStatus.BAD_GATEWAY);
                            })
                    .body(GatewayPayoutDetailsResponse.class);
            if (response==null){
                throw new ApiException("Empty response received from Razorpay.",
                        HttpStatus.BAD_GATEWAY);
            }
            log.info("Fetched Razorpay payout successfully. Gateway Payout Id: {}", response.getId());
            return response;
        }
        catch (ApiException exception) {

            throw exception;

        }
        catch (RestClientException exception) {

            log.error(
                    "Failed to fetch Razorpay payout.",
                    exception);

            throw new ApiException(
                    "Failed to fetch Razorpay payout.",
                    HttpStatus.BAD_GATEWAY);

        }
        catch (Exception exception) {

            log.error(
                    "Failed to parse Razorpay payout response.",
                    exception);

            throw new ApiException(
                    "Failed to parse Razorpay payout response.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
