package org.example.auth.model.enums;

public enum AccountStatus {
    PENDING_VERIFICATION, // User created, OTP not yet verified
    ACTIVE,               // Verified and able to login
    LOCKED,               // Too many failed login/OTP attempts
    SUSPENDED             // Blocked by Admin
}