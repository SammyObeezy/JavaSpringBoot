package org.example.escrow.repository;

import org.example.escrow.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    // Find the specific active code for verification
    Optional<OtpCode> findByUserIdAndCodeAndUsedFalse(UUID userId, String code);

    // Find ALL active codes for a user (used for invalidation)
    List<OtpCode> findByUserIdAndUsedFalse(UUID userId);
}