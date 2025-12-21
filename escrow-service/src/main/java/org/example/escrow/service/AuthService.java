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

    // Step 1: Check credentials, send OTP, return void for message
    void initiateLogin(LoginRequest request);

    // Step 2: Check OTP, return Token
    AuthResponse verifyLogin(VerifyOtpRequest request);
}
