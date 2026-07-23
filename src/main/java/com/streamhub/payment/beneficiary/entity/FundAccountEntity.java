package com.streamhub.payment.beneficiary.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.beneficiary.enums.FundAccountStatus;
import com.streamhub.payment.beneficiary.enums.FundAccountType;
import com.streamhub.payment.common.entity.BaseEntity;
import com.streamhub.payment.payment.enums.PaymentGateway;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "fund_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                name = "uk_fund_account_reference",
                columnNames = "fund_account_reference" ),
                @UniqueConstraint(
                        name = "uk_gateway_fund_account",
                        columnNames = "gateway_fund_account_id")
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundAccountEntity extends BaseEntity {

    @Column(name = "fund_account_reference",nullable = false,length = 26)
    private String fundAccountReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "beneficiary_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fund_account_beneficiary")
    )
    private BeneficiaryEntity beneficiary;

    @Enumerated(EnumType.STRING)
    @Column(name = "fund_account_type",
            nullable = false,
            length = 30
    )
    private FundAccountType fundAccountType;

    @Column(name = "account_holder_name",length = 150)
    private String accountHolderName;

    @Column(name = "account_number",length = 50)
    private String accountNumber;
    @Column(name = "ifsc_code",length = 20)
    private String ifscCode;

    @Column(length = 100)
    private String vpa;

    @Column(name = "gateway_fund_account_id",length = 100)
    private String gatewayFundAccountId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "gateway_response", columnDefinition = "jsonb")
    private JsonNode gatewayResponse;

    @Column(name = "is_default",nullable = false)
    private Boolean isDefault;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false,length = 30)
    private FundAccountStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

}
