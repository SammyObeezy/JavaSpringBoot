package org.example.escrow.repository;

import org.example.escrow.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    // For User Statements (Transaction History)
    List<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    // Integrity Check: Find both legs (Debit/Credit) of a transaction
    List<LedgerEntry> findByTransactionId(UUID transactionId);
}
