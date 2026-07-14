package com.streamhub.payment.gateway.util;

import com.streamhub.payment.payment.enums.PaymentMethod;

public final class RazorpayPaymentMethodMapper {

    public static PaymentMethod map(String method){
        return  switch (method.toLowerCase()){
            case "upi" -> PaymentMethod.UPI;
            case "card" -> PaymentMethod.CARD;
            case "netbanking" ->PaymentMethod.NET_BANKING;
            case "wallet" -> PaymentMethod.WALLET;
            case "emi" -> PaymentMethod.EMI;
            default -> PaymentMethod.UNKNOWN;

        };
    }
}
