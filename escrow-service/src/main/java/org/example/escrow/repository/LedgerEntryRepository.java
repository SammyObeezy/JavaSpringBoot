package org.example.escrow.repository;

import org.example.escrow.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("SELECT l FROM LedgerEntry l " +
            "JOIN FETCH l.wallet " +
            "WHERE l.wallet.id = :walletId " +
            "ORDER BY l.createdAt DESC")
    List<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(@Param("walletId") UUID walletId);

    @Query("SELECT l FROM LedgerEntry l WHERE l.transactionId = :transactionId")
    List<LedgerEntry> findByTransactionId(@Param("transactionId") UUID transactionId);
}