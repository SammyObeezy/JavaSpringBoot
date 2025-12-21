package org.example.escrow.service;

import org.example.escrow.dto.identity.AuthResponse;
import org.example.escrow.dto.identity.RegisterRequest;

public interface AuthService {
    /**
     * Registers a new user and creates their default wallet.
     * @param request The registration details (email, phone, password).
     * @return The created user details (without sensitive data).
     */
    AuthResponse register(RegisterRequest request);
}
