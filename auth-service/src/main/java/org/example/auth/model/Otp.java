package org.example.auth.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "otps")
public class Otp extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String otpCode; // Encrypted in real prod, Hashed here for demo

    @Column(nullable = false)
    private String userEmail; // Link to user

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private int attempts;
    private boolean used;

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
