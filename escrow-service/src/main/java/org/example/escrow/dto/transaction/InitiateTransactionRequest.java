package org.example.escrow.dto.transaction;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class InitiateTransactionRequest {

    @NotNull(message = "Service ID is required")
    private UUID serviceId;
}
