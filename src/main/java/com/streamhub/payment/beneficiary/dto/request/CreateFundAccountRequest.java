package com.streamhub.payment.beneficiary.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.streamhub.payment.beneficiary.enums.FundAccountType;
import com.streamhub.payment.beneficiary.enums.OwnerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;


@Builder
public record CreateFundAccountRequest(
        @NotBlank(message = "Owner reference is required.")
        @Size(max = 26,message = "Owner reference must not exceed 26 characters")
        String ownerReference,

        @NotNull(message = "Owner type is required.")
        OwnerType ownerType,

        @NotBlank(message = "Name is required.")
        @Size(max = 150,message = "Name must not exceed 150 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email address.")
        @Size(max = 255,message = "Email must not exceed 255 characters.")
        String email,

        @NotBlank(message = "Contact number is required.")
        @Size(max = 20,message = "Contact number must not exceed 20 characters.")
        String contactNumber,

        @NotNull(message = "Fund account type is required.")
        FundAccountType fundAccountType,

        @Size(max = 150,message = "Account holder name must not exceed 150 characters.")
        String accountHolderName,

        @Size(max = 50,message = "Account number must not exceed 50 characters.")
        String accountNumber,

        @Size(max = 20,message = "IFSC code must not exceed 20 characters.")
        String ifscCode,

        @Size(max = 100,message = "VPA must not exceed 100 characters.")
        String vpa,

//        @NotNull(message = "Default flag is required.")
        Boolean isDefault,

        JsonNode metadata


) {


}
