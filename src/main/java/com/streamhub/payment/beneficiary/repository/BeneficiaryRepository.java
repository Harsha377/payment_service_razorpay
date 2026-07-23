package com.streamhub.payment.beneficiary.repository;

import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.enums.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BeneficiaryRepository extends JpaRepository<BeneficiaryEntity,Long> {

    Optional<BeneficiaryEntity> findByBeneficiaryReference(String beneficiaryReference);
    Optional<BeneficiaryEntity> findByOwnerReferenceAndOwnerType(String ownerReference, OwnerType ownerType);
    Optional<BeneficiaryEntity> findByGatewayContactId(String gatewayContactId);
}
