package com.streamhub.payment.beneficiary.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.beneficiary.enums.BeneficiaryStatus;
import com.streamhub.payment.beneficiary.enums.OwnerType;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.payment.enums.PaymentGateway;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "beneficiaries",uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_beneficiary_reference",
                columnNames = "beneficiary_reference"
        ),
        @UniqueConstraint(
                name = "uk_beneficiary_owner",
                columnNames = {
                        "owner_reference",
                        "owner_type"
                }
        ),
        @UniqueConstraint(
                name = "uk_gateway_contact_id",
                columnNames = "gateway_contact_id"
        )
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryEntity extends BaseEntity {

    @Column(name = "beneficiary_reference",nullable = false,length = 26)
    private String beneficiaryReference;

    @Column(name = "owner_reference",nullable = false,length = 26)
    private String ownerReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type",nullable = false,length = 30)
    private OwnerType ownerType;

    @Column(name = "name", nullable = false,length = 150)
    private String name;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "contact_number",nullable = false,length = 20)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_name",nullable = false,length = 50)
    private PaymentGateway gatewayName;

    @Column(name = "gateway_contact_id",length = 100)
    private String gatewayContactId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false,length = 30)
    private BeneficiaryStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private JsonNode gatewayResponse;

    @Builder.Default
    @OneToMany(
            mappedBy = "beneficiary",
            fetch = FetchType.LAZY
    )
    private List<FundAccountEntity> fundAccounts=new ArrayList<>();
}
