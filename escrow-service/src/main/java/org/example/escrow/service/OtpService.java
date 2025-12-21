package org.example.escrow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.OtpCode;
import org.example.escrow.model.User;
import org.example.escrow.repository.OtpCodeRepository;
import org.example.escrow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${app.security.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${app.security.otp.length:6}")
    private int otpLength;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a dynamic length OTP, saves it, and sends it via SMS.
     */
    @Transactional
    public void generateAndSendOtp(User user){
        int min = (int) Math.pow(10, otpLength - 1);
        int range = (int) Math.pow(10, otpLength) - min;
        String code = String.valueOf(min + secureRandom.nextInt(range));

        OtpCode otp = OtpCode.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .used(false)
                .build();

        otpCodeRepository.save(otp);
        log.info("Generated OTP for use {}", user.getId());

        String message = "Your Escrow Verification Code is: " + code + ". Valid for " + otpExpiryMinutes + " minutes.";
        notificationService.sendSms(user.getPhoneNumber(), message);
    }
    /**
     * Verifies the OTP. If valid, marks User as phone verified.
     */
    @Transactional
    public void verifyOtp(String email, String code){
        // 1. Find User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (user.isPhoneVerified()){
            return;
        }

        // 2. Find OTP using YOUR existing repository method
        OtpCode otp = otpCodeRepository.findByUserIdAndCodeAndUsedFalse(user.getId(), code)
                .orElseThrow(() -> new BusinessLogicException("Invalid OP code"));

        // 3. Manual Expiry Check (Since repo query doesn't check time)
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("OTP has expired");
        }

        // 4. Mark used
        otp.setUsed(true);
        otpCodeRepository.save(otp);

        // 5. Update User
        user.setPhoneVerified(true);
        userRepository.save(user);
        log.info("Phone verified for user {}", user.getId());
    }
}
