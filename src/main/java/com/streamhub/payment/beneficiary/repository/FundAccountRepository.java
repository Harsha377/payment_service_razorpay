package com.streamhub.payment.beneficiary.repository;

import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import com.streamhub.payment.beneficiary.entity.FundAccountEntity;
import com.streamhub.payment.beneficiary.enums.FundAccountStatus;
import com.streamhub.payment.beneficiary.enums.FundAccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FundAccountRepository extends JpaRepository<FundAccountEntity,Long> {

    Optional<FundAccountEntity> findByFundAccountReference(String fundAccountReference);
    List<FundAccountEntity> findAllByBeneficiary(BeneficiaryEntity beneficiary);
    Optional<FundAccountEntity> findByBeneficiaryAndIsDefaultTrue(BeneficiaryEntity beneficiary);
    Optional<FundAccountEntity> findByGatewayFundAccountId(String gatewayFundAccountId);
    List<FundAccountEntity> findAllByBeneficiaryAndFundAccountType(
            BeneficiaryEntity beneficiary,
            FundAccountType fundAccountType
    );
    Optional<FundAccountEntity> findByBeneficiaryAndFundAccountType(
            BeneficiaryEntity beneficiary,
            FundAccountType fundAccountType);

    /**
     * Get all fund accounts of a beneficiary.
     */
    List<FundAccountEntity> findByBeneficiaryOrderByCreatedAtDesc(
            BeneficiaryEntity beneficiary);

    boolean existsByBeneficiaryAndIsDefaultTrue(BeneficiaryEntity beneficiary);

    List<FundAccountEntity> findByBeneficiaryAndStatusOrderByCreatedAtAsc(BeneficiaryEntity beneficiary, FundAccountStatus fundAccountStatus);
}
