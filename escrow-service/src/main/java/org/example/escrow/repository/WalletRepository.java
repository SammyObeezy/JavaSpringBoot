package org.example.escrow.repository;

import org.example.escrow.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    @Query("SELECT w FROM Wallet w JOIN FETCH w.user WHERE w.user.id = :userId AND w.currency = :currency")
    Optional<Wallet> findByUserIdAndCurrency(@Param("userId") UUID userId, @Param("currency") String currency);
}