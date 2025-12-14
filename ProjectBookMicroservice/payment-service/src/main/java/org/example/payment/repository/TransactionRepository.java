package org.example.payment.repository;

import org.example.payment.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByCheckoutRequestId(String checkoutRequestId);
    Optional<Transaction> findByBookingId(Long bookingId);
}