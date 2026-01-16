package org.example.escrow.service;

import org.example.escrow.model.User;
import org.example.escrow.model.enums.NotificationChannel;

public interface OtpService {
    void generateAndSendOtp(User user, NotificationChannel channel);

    void generateAndSendOtp(User user);

    void resendOtp(String email, NotificationChannel channel);

    void verifyOtp(String email, String code);
}