package org.example.gateway.repository;

import org.example.gateway.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByCheckoutRequest(String checkoutRequestID);
    Optional<Transaction> findByBookingId(Long bookingId);
}
