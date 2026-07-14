package com.streamhub.payment.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ApiException  extends  RuntimeException{
    private final HttpStatus status;
    private final Map<String, String> errors;

    public ApiException(String message,HttpStatus status){
        super(message);
        this.status=status;
        this.errors=null;
    }
    public ApiException(String message, HttpStatus status, Map<String, String> errors){
        super(message);
        this.status=status;
        this.errors=errors;
    }
}
