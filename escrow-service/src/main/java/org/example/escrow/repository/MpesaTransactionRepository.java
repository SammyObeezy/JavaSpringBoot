package org.example.escrow.repository;

import org.example.escrow.model.MpesaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MpesaTransactionRepository extends JpaRepository<MpesaTransaction, UUID> {
    @Query("SELECT t FROM MpesaTransaction t JOIN FETCH t.user WHERE t.checkoutRequestId = :checkoutRequestId")
    Optional<MpesaTransaction> findByCheckoutRequestId(@Param("checkoutRequestId") String checkoutRequestId);
}