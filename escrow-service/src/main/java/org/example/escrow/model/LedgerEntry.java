package org.example.escrow.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.escrow.model.enums.LedgerEntryType;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry extends  BaseEntity{

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;
}
