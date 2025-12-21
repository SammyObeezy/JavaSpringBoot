package org.example.escrow.repository;

import org.example.escrow.model.EscrowTransaction;
import org.example.escrow.model.enums.EscrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, UUID> {

    // "My Orders" (As a Buyer)
    // Fixed: Changed findByBuyerId to findByBuyId to match the 'private User buy' field in the Entity
    List<EscrowTransaction> findByBuyIdOrderByCreatedAtDesc(UUID buyerId);

    // "My Sales" (As a Merchant)
    // This works because the field is named 'private MerchantProfile merchant'
    List<EscrowTransaction> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    // For Cron Jobs: Find stuck transactions (e.g., waiting payment too long)
    List<EscrowTransaction> findByStatus(EscrowStatus status);
}