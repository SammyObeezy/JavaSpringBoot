package org.example.escrow.repository;

import org.example.escrow.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    // Critical: Find the specific currency wallet for a user
    Optional<Wallet> findByUserIdAndCurrency (UUID userId, String currency);
}
