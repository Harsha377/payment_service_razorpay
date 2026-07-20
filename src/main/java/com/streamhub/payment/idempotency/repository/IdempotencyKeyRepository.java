package com.streamhub.payment.idempotency.repository;

import com.streamhub.payment.idempotency.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, Long> {
    Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotency);

    @Modifying
    @Query(
            """
            DELETE FROM IdempotencyKeyEntity i
            WHERE i.expiresAt < :currentTime""")
    int deleteExpiredKeys(OffsetDateTime currentTime);
}
