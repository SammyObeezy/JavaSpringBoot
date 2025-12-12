package org.example.auth.repository;

import org.example.auth.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long>{

    // Find latest unused OPT from user
    Optional<Otp> findTopByUserEmailAndUsedFalseOrderByCreatedAtDesc(String email);

}

