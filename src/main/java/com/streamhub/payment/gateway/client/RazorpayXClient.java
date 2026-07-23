package com.streamhub.payment.gateway.client;

import com.streamhub.payment.gateway.dto.request.GatewayContactRequest;
import com.streamhub.payment.gateway.dto.request.GatewayFundAccountRequest;
import com.streamhub.payment.gateway.dto.response.GatewayContactResult;
import com.streamhub.payment.gateway.dto.response.GatewayFundAccountResult;

public interface RazorpayXClient {
    GatewayContactResult createContact(GatewayContactRequest request);
    GatewayFundAccountResult createFundAccount(GatewayFundAccountRequest request);
}
