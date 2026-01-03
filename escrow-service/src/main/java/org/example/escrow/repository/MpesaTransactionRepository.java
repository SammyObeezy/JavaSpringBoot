package org.example.escrow.repository;

import org.example.escrow.model.MpesaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MpesaTransactionRepository extends JpaRepository<MpesaTransaction, UUID> {
    Optional<MpesaTransaction> findByCheckoutRequestId(String checkoutRequestId);
}