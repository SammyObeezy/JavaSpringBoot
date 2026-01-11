package org.example.escrow.dto.admin;

import lombok.Builder;
import lombok.Data;
import org.example.escrow.model.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private UserRole role;
    private boolean active;
    private boolean phoneVerified;
    private LocalDateTime createdAt;
}