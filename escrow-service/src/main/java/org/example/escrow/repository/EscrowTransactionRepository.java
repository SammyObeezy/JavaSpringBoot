package org.example.escrow.repository;

import org.example.escrow.model.EscrowTransaction;
import org.example.escrow.model.enums.EscrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, UUID> {

    // "My Orders" (As a Buyer)
    List<EscrowTransaction> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);

    // "My Sales" (As a Merchant)
    List<EscrowTransaction> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    // For Cron Jobs: Find stuck transactions (e.g., waiting payment too long)
    List<EscrowTransaction> findByStatus(EscrowStatus status);
}
