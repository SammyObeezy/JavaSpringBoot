package org.example.escrow.repository;

import org.example.escrow.model.MerchantService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MerchantServiceRepository extends JpaRepository<MerchantService, UUID> {

    @Query("SELECT s FROM MerchantService s JOIN FETCH s.merchant WHERE s.merchant.id = :merchantId AND s.active = true")
    List<MerchantService> findByMerchantIdAndActiveTrue(@Param("merchantId") UUID merchantId);
}