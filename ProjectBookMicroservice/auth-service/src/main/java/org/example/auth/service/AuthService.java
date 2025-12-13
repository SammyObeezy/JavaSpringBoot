package org.example.auth.service;

import org.example.auth.dto.RegisterRequest;
import org.example.auth.model.User;

public interface AuthService {
    User registerUser(RegisterRequest request);
    void verifyOtp(String email, String otpCode);
    String login(String email, String password);
    void resendOtp(String email);
}
