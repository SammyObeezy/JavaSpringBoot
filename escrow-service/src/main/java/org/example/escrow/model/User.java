package org.example.escrow.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.escrow.model.enums.UserRole;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity{

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.ROLE_USER;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_phone_verified")
    @Builder.Default
    private boolean phoneVerified = false;
}
