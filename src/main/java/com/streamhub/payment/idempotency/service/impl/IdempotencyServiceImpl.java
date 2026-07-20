package com.streamhub.payment.idempotency.service.impl;

import com.streamhub.payment.common.exception.ConflictException;
import com.streamhub.payment.idempotency.entity.IdempotencyKeyEntity;
import com.streamhub.payment.idempotency.enums.ResourceType;
import com.streamhub.payment.idempotency.repository.IdempotencyKeyRepository;
import com.streamhub.payment.idempotency.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyServiceImpl  implements IdempotencyService {
    private static final long EXPIRATION_HOURS = 24;

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    @Override
    public Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotency) {
        return idempotencyKeyRepository.findByIdempotencyKey(idempotency);
    }
    @Override
    public void validateRequest(IdempotencyKeyEntity entity, String requestHash) {

        if (!entity.getRequestHash().equals(requestHash)){
            throw new ConflictException("The Idempotency-Key has already been used with a different request.");
        }
    }

//  This method is responsible for persisting the idempotency record after the business operation succeeds.
    @Override
    public void save(String idempotencyKey, String requestHash, ResourceType resourceType, String resourceReference, Integer httpStatus) {
        OffsetDateTime now=OffsetDateTime.now();
        IdempotencyKeyEntity entity=new IdempotencyKeyEntity();
        entity.setIdempotencyKey(idempotencyKey);
        entity.setRequestHash(requestHash);
        entity.setResourceType(resourceType);
        entity.setResourceReference(resourceReference);
        entity.setHttpStatus(httpStatus);
        entity.setExpiresAt(now.plusHours(EXPIRATION_HOURS));

        idempotencyKeyRepository.save(entity);
    }

    public void deleteExpiredKeys(){
        int deletedCount=idempotencyKeyRepository.deleteExpiredKeys(OffsetDateTime.now());
        log.info("Deleted {} expired idempotency keys.", deletedCount);
    }

    @Override
    public boolean isExpired(IdempotencyKeyEntity entity) {
        return entity.getExpiresAt().isBefore(OffsetDateTime.now());
    }

    @Override
    @Transactional
    public void delete(IdempotencyKeyEntity idempotencyKey) {

        log.info("Deleting expired idempotency key. Idempotency Key: {}",
                idempotencyKey.getIdempotencyKey());

        idempotencyKeyRepository.delete(idempotencyKey);

        log.info("Expired idempotency key deleted successfully. Idempotency Key: {}",
                idempotencyKey.getIdempotencyKey());
    }
}
