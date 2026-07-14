package com.streamhub.payment.common.exception;

import org.springframework.http.HttpStatus;
//ResourceNotFoundException (404)

public class ResourceNotFoundException extends ApiException{
    public ResourceNotFoundException(String message){
        super(message, HttpStatus.NOT_FOUND);
    }
}