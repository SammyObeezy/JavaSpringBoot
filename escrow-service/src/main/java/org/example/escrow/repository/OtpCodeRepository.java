package org.example.escrow.repository;

import org.example.escrow.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    @Query("SELECT o FROM OtpCode o WHERE o.user.id = :userId AND o.code = :code AND o.used = false")
    Optional<OtpCode> findByUserIdAndCodeAndUsedFalse(@Param("userId") UUID userId, @Param("code") String code);

    @Query("SELECT o FROM OtpCode o WHERE o.user.id = :userId AND o.used = false")
    List<OtpCode> findByUserIdAndUsedFalse(@Param("userId") UUID userId);
}