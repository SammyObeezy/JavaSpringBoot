package org.example.escrow.dto.wallet;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10.00", message = "Minimum deposit is 10.00")
    private BigDecimal amount;

    private String currency;
}
