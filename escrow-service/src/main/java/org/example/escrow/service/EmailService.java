package org.example.escrow.service;

public interface EmailService {
    void sendEmail(String toEmail, String subject, String body);
}