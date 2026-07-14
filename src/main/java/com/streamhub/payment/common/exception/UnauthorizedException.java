package com.streamhub.payment.common.exception;

import org.springframework.http.HttpStatus;

//UnauthorizedException (401)
public class UnauthorizedException extends  ApiException {
    public UnauthorizedException(String message){
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
