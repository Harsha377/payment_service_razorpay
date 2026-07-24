package com.streamhub.payment.beneficiary.dto.response;

import com.streamhub.payment.beneficiary.entity.BeneficiaryEntity;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeneficiaryPageResponse {
    private List<BeneficiaryResponse> beneficiaries;
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;
}
