package com.streamhub.payment.common.exception;

import org.springframework.http.HttpStatus;

//ConflictException (409)
public class ConflictException extends ApiException{
    public ConflictException(String message){
        super(message, HttpStatus.CONFLICT);
    }
}
