package org.example.escrow.dto.wallet;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class WalletResponse {
    private UUID walletId;
    private BigDecimal balance;
    private String currency;
    private String ownerName; // We can map "John Doe" here conveniently
}