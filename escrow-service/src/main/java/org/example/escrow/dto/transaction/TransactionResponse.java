package org.example.escrow.dto.transaction;

import lombok.Builder;
import lombok.Data;
import org.example.escrow.model.enums.EscrowStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {
    private UUID transactionId;
    private String serviceName;
    private String merchantName;
    private EscrowStatus status;

    // Financials
    private BigDecimal itemPrice;
    private BigDecimal platformFee;
    private BigDecimal totalToPay;
    private String currency;

    private LocalDateTime createdAt;
}
