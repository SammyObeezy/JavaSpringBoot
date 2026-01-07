package org.example.escrow.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.escrow.model.enums.EscrowStatus;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "escrow_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Switched back to @Builder as requested
public class EscrowTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private MerchantProfile merchant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private MerchantService service;

    // Financial Breakdown
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "platform_fee", nullable = false, precision = 19, scale = 4)
    private BigDecimal platformFee;

    @Column(name = "merchant_payout", nullable = false, precision = 19, scale = 4)
    private BigDecimal merchantPayout;

    @Column(length = 3)
    @Builder.Default
    private String currency = "KES";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EscrowStatus status = EscrowStatus.CREATED;

    @Column(name = "funding_transaction_id")
    private UUID fundingTransactionId;
}