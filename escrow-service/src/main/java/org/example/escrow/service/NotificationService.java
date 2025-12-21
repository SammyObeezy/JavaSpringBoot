package org.example.escrow.service;

public interface NotificationService {
    /**
     * Sends an OTP via SMS
     * @param phoneNumber The recipient's phone number (e.g., +2547...)
     * @param message The actual message content.
     */
    void sendSms(String phoneNumber, String message);
}
