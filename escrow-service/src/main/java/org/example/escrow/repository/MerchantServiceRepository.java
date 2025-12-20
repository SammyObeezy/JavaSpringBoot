package org.example.escrow.repository;

import org.example.escrow.model.MerchantService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MerchantServiceRepository extends JpaRepository<MerchantService, UUID> {

    // For public Marketplace: Show active service for a merchant
    List<MerchantService> findByMerchantIdAndActiveTrue(UUID merchantId);
}
