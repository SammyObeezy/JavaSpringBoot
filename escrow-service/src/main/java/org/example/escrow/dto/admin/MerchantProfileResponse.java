package org.example.escrow.dto.admin;

import lombok.Builder;
import lombok.Data;
import org.example.escrow.model.enums.VerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MerchantProfileResponse {
    private UUID id;
    private String businessName;
    private String businessRegNo;
    private BigDecimal commissionRate;
    private VerificationStatus verificationStatus;

    // Flattened User Data for easy display
    private UUID userId;
    private String ownerName;
    private String ownerEmail;

    private LocalDateTime createdAt;
}