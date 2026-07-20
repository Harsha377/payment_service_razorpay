package com.streamhub.payment.idempotency.service;

import com.streamhub.payment.idempotency.entity.IdempotencyKeyEntity;
import com.streamhub.payment.idempotency.enums.ResourceType;

import java.util.Optional;

public interface IdempotencyService {

    Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotency);

    void validateRequest(IdempotencyKeyEntity entity,String requestHash);

    void save(String idempotencyKey,
              String requestHash,
              ResourceType resourceType,
              String resourceReference,
              Integer httpStatus
                              );

    void deleteExpiredKeys();

    boolean isExpired(IdempotencyKeyEntity entity);

    void delete(IdempotencyKeyEntity idempotencyKeyEntity);
}
