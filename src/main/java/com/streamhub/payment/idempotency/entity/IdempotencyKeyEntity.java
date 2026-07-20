package com.streamhub.payment.idempotency.entity;

import com.streamhub.payment.common.entity.EventBaseEntity;
import com.streamhub.payment.idempotency.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "idempotency_keys",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_idempotency_keys_key",
                        columnNames = "idempotency_key"
                )
        }
)
public class IdempotencyKeyEntity extends EventBaseEntity {

    @Column(name = "idempotency_key",nullable = false,length = 255)
    private String idempotencyKey;

    @Column(name = "request_hash",nullable = false,length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type",nullable = false,length = 50)
    private ResourceType resourceType;

    @Column(name = "resource_reference",nullable = false,length = 26)
    private String resourceReference;

    @Column(name = "http_status",nullable = false)
    private Integer httpStatus;

    @Column(name = "expires_at",nullable = false)
    private OffsetDateTime expiresAt;
}
