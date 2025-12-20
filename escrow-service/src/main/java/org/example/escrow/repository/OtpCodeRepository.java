package org.example.escrow.repository;

import org.example.escrow.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    // Find a valid, unused code for a user
    Optional<OtpCode> findByUserIdAndCodeAndUsedFalse (UUID userId, String code);

    // Clean up: Find expires codes (for a scheduled job deletion)
    void deleteByExpiresAtBefore(LocalDateTime now);
}
