package org.example.escrow.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantOnboardingRequest {

    @NotBlank(message = "Business name is required")
    private String businessName;

    // This field was likely missing or named differently
    private String businessRegNo;
}