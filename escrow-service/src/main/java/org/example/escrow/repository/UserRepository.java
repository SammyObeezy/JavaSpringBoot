package org.example.escrow.repository;

import org.example.escrow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    // Efficient existence checks (optimized queries)
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
