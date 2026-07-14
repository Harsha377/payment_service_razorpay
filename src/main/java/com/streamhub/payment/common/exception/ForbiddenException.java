package com.streamhub.payment.common.exception;

import org.springframework.http.HttpStatus;
//ForbiddenException (403)

public class ForbiddenException extends ApiException{
    public ForbiddenException(String message){
        super(message, HttpStatus.FORBIDDEN);
    }
}
