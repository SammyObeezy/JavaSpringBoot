package org.example.auth.service;

public interface NotificationService {
    void sendSms(String phoneNumber, String message);
    void sendEmail(String toEmail, String subject, String body);
}
