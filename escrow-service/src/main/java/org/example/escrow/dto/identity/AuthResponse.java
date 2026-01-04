package org.example.escrow.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private UUID userId;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean phoneVerified;
    private boolean kycVerified;

    // In a real JWT setup, this would be the Access Token
    private String accessToken;
}