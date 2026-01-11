package org.example.escrow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.escrow.config.AppProperties;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.OtpCode;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.NotificationChannel;
import org.example.escrow.repository.OtpCodeRepository;
import org.example.escrow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final NotificationService smsService; // This is AwsSnsService
    private final EmailService emailService;
    private final AppProperties appProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a new OTP, invalidates old ones, and sends it via the requested channel.
     */
    @Transactional
    public void generateAndSendOtp(User user, NotificationChannel channel) {
        // 1. Invalidate ALL existing valid OTPs for this user
        // This ensures the old code expires immediately
        List<OtpCode> activeCodes = otpCodeRepository.findByUserIdAndUsedFalse(user.getId());
        if (!activeCodes.isEmpty()) {
            activeCodes.forEach(otp -> otp.setUsed(true));
            otpCodeRepository.saveAll(activeCodes);
            log.info("Invalidated {} old OTP codes for user {}", activeCodes.size(), user.getEmail());
        }

        // 2. Generate new Code
        int otpLength = appProperties.getSecurity().getOtpLength(); // e.g. 6
        int otpExpiry = (int) appProperties.getSecurity().getOtpExpirationMinutes(); // e.g. 5

        // Generate dynamic length code (e.g. 100000 to 999999)
        int min = (int) Math.pow(10, otpLength - 1);
        int range = (int) Math.pow(10, otpLength) - min;
        String code = String.valueOf(min + secureRandom.nextInt(range));

        // 3. Save New OTP
        OtpCode otp = OtpCode.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiry))
                .used(false)
                .build();

        otpCodeRepository.save(otp);

        // 4. Send via selected Channel
        String message = "Your Escrow Verification Code is: " + code + ". Valid for " + otpExpiry + " minutes.";

        if (channel == NotificationChannel.EMAIL) {
            emailService.sendEmail(user.getEmail(), "Escrow Verification Code", message);
        } else {
            // Default to SMS
            smsService.sendSms(user.getPhoneNumber(), message);
        }

        log.info("Generated new OTP for user {} via {}", user.getId(), channel);
    }

    // Overload for default behavior (SMS)
    @Transactional
    public void generateAndSendOtp(User user) {
        generateAndSendOtp(user, NotificationChannel.SMS);
    }

    /**
     * Resend Logic exposed to Controller
     */
    @Transactional
    public void resendOtp(String email, NotificationChannel channel) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isPhoneVerified()) {
            // Stop process if already verified. User should login instead.
            throw new BusinessLogicException("Phone number is already verified. Please login.");
        }

        generateAndSendOtp(user, channel);
    }

    /**
     * Verifies the OTP.
     */
    @Transactional
    public void verifyOtp(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        OtpCode otp = otpCodeRepository.findByUserIdAndCodeAndUsedFalse(user.getId(), code)
                .orElseThrow(() -> new BusinessLogicException("Invalid OTP code"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("OTP has expired");
        }

        otp.setUsed(true);
        otpCodeRepository.save(otp);

        if (!user.isPhoneVerified()) {
            user.setPhoneVerified(true);
            userRepository.save(user);
        }
    }
}