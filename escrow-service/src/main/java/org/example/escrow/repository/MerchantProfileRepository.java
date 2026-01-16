package org.example.escrow.repository;

import org.example.escrow.model.MerchantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, UUID> {

    @Query("SELECT m FROM MerchantProfile m JOIN FETCH m.user WHERE m.user.id = :userId")
    Optional<MerchantProfile> findByUserId(@Param("userId") UUID userId);

    boolean existsByBusinessRegNo(String businessRegNo);
}