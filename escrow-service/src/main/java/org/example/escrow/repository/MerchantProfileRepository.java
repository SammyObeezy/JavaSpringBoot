package org.example.escrow.repository;

import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, UUID> {

    Optional<MerchantProfile> findByUserId(UUID userId);

    // For Admin Dashboard: Find all pending merchants
    List<MerchantProfile> findByVerificationStatus(VerificationStatus status);
}
