package org.example.escrow.service;

import org.example.escrow.dto.identity.AuthResponse;
import org.example.escrow.dto.identity.LoginRequest;
import org.example.escrow.dto.identity.RegisterRequest;
import org.example.escrow.dto.identity.VerifyOtpRequest;

public interface AuthService {
    /**
     * Registers a new user and creates their default wallet.
     * @param request The registration details (email, phone, password).
     * @return The created user details (without sensitive data).
     */
    AuthResponse register(RegisterRequest request);

    void initiateLogin(LoginRequest request);

    AuthResponse verifyLogin(VerifyOtpRequest request);

    void logout(String token);
}