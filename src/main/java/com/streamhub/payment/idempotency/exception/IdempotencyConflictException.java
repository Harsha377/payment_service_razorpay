package com.streamhub.payment.idempotency.exception;

import com.streamhub.payment.common.exception.ConflictException;

public class IdempotencyConflictException extends ConflictException {
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
