package org.example.escrow.repository;

import org.example.escrow.model.MerchantProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, UUID> {

    // Find profile linked to a specific user
    Optional<MerchantProfile> findByUserId(UUID userId);

    // Check for duplicate business registration numbers
    boolean existsByBusinessRegNo(String businessRegNo);
}