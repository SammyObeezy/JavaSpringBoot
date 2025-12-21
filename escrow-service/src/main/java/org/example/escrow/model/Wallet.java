package org.example.escrow.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.escrow.model.enums.WalletType;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "currency"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends BaseEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(length = 3, nullable = false)
    @Builder.Default
    private String currency = "KES";

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type")
    @Builder.Default
    private WalletType walletType = WalletType.PERSONAL;
}
