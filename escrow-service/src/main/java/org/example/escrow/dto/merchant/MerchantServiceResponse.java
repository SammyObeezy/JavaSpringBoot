package org.example.escrow.dto.merchant;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class MerchantServiceResponse {
    private UUID serviceId;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private boolean active;
}