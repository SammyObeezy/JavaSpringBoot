package org.example.escrow.dto.identity;


import lombok.Data;

import java.util.UUID;

@Data
public class AuthResponse {

    private UUID userId;
    private String email;
    private String phoneNumber;
    private String role;
    private boolean isPhoneVerified;
    private boolean isKycVerified;

    // In a real JWT setup, this would be the Access Token
    private String accessToken;
}
