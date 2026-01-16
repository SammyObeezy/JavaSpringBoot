package org.example.escrow.repository;

import org.example.escrow.model.EscrowTransaction;
import org.example.escrow.model.enums.EscrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, UUID> {

    @Query("SELECT t FROM EscrowTransaction t " +
            "JOIN FETCH t.service " +
            "JOIN FETCH t.merchant " +
            "WHERE t.buy.id = :buyerId " +
            "ORDER BY t.createdAt DESC")
    List<EscrowTransaction> findByBuyIdOrderByCreatedAtDesc(@Param("buyerId") UUID buyerId);

    @Query("SELECT t FROM EscrowTransaction t " +
            "JOIN FETCH t.service " +
            "JOIN FETCH t.buy " +
            "WHERE t.merchant.id = :merchantId " +
            "ORDER BY t.createdAt DESC")
    List<EscrowTransaction> findByMerchantIdOrderByCreatedAtDesc(@Param("merchantId") UUID merchantId);

    List<EscrowTransaction> findByStatus(EscrowStatus status);
}