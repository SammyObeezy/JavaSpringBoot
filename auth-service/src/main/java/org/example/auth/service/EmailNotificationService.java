package org.example.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Primary // <--- This tells Spring: "Use THIS one, ignore the AWS one for now"
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    public void sendSms(String phoneNumber, String message) {
        // Fallback: Since we can't send SMS for free, we log it.
        // In a real app, you might map phone numbers to emails or just log here.
        log.info("SKIPPING SMS (Free Tier): Would send '{}' to {}", message, phoneNumber);
    }

    @Override
    public void sendEmail(String toEmail, String subject, String body) {
        log.info("Sending Email OTP to {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@safaricom-events.com");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully.");
        } catch (Exception e) {
            log.error("Failed to send email", e);
            // Don't crash the user registration if email fails in dev, just log the OTP
            throw new RuntimeException("Email service failed");
        }
    }
}