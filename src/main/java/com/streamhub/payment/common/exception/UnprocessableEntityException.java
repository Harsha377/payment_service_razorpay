package com.streamhub.payment.common.exception;

import org.springframework.http.HttpStatus;
//UnprocessableEntityException (422)
public class UnprocessableEntityException extends ApiException{
    public UnprocessableEntityException(String message){
        super(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
