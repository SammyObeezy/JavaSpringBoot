package org.example.escrow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.example.escrow.model.enums.EscrowStatus;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "escrow_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EscrowTransaction extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false, updatable = false)
    private User buy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false, updatable = false)
    private MerchantProfile merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false, updatable = false)
    private MerchantService service;

    // Financial Breakdown (Immutable: Financial history must remain static)
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal totalAmount;

    @Column(name = "platform_fee", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal platformFee;

    @Column(name = "merchant_payout", nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal merchantPayout;

    @Column(length = 3, nullable = false, updatable = false)
    @Builder.Default
    private String currency = "KES";

    // Mutable State (This is the only thing that changes lifecycle)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EscrowStatus status = EscrowStatus.CREATED;

    @Column(name = "funding_transaction_id")
    private UUID fundingTransactionId;

    // Optimistic Locking to prevent concurrent status updates
    @Version
    private Long version;
}