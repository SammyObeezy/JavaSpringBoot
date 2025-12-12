package org.example.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.auth.dto.RegisterRequest;
import org.example.auth.model.Otp;
import org.example.auth.model.User;
import org.example.auth.model.enums.AccountStatus;
import org.example.auth.model.enums.UserRole;
import org.example.auth.repository.OtpRepository;
import org.example.auth.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final NotificationService notificationService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                throw new RuntimeException("Phone number already in use");
            }

            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setRole(UserRole.CUSTOMER);
            user.setStatus(AccountStatus.PENDING_VERIFICATION);
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            User savedUser = userRepository.save(user);

            generateAndSendOtp(savedUser);

            return savedUser;

        } catch (Exception e) {
            log.error("Registration error for {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void verifyOtp(String email, String otpCode) {
        try {
            Otp otp = otpRepository.findTopByUserEmailAndUsedFalseOrderByCreatedAtDesc(email)
                    .orElseThrow(() -> new RuntimeException("No valid OTP found"));

            if (otp.isExpired()) {
                throw new RuntimeException("OTP has expired");
            }

            if (otp.getAttempts() >= 3) {
                lockUserAccount(email);
                throw new RuntimeException("Too many failed attempts. Account locked.");
            }

            if (!otp.getOtpCode().equals(otpCode)) {
                otp.setAttempts(otp.getAttempts() + 1);
                otpRepository.save(otp);
                throw new RuntimeException("Invalid OTP code");
            }

            otp.setUsed(true);
            otpRepository.save(otp);

            activateUser(email);

            log.info("User {} verified successfully", email);

        } catch (Exception e) {
            log.error("OTP verification error for {}: {}", email, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void login(String email, String password) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (user.getStatus() == AccountStatus.LOCKED) {
                throw new RuntimeException("Account is locked");
            }

            if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
                throw new RuntimeException("Account not verified");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                handleFailedLogin(user);
                throw new RuntimeException("Invalid credentials");
            }

            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("User logged in: {}", email);

        } catch (Exception e) {
            log.error("Login failed for {}: {}", email, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void resendOtp(String email) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getStatus() == AccountStatus.LOCKED) {
                throw new RuntimeException("Account is locked");
            }

            generateAndSendOtp(user);
            log.info("OTP resent to {}", email);

        } catch (Exception e) {
            log.error("Resend OTP error for {}: {}", email, e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    private void generateAndSendOtp(User user) {
        int code = 100000 + secureRandom.nextInt(900000);
        String otpValue = String.valueOf(code);

        Otp otp = new Otp();
        otp.setOtpCode(otpValue);
        otp.setUserEmail(user.getEmail());
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setAttempts(0);
        otp.setUsed(false);

        otpRepository.save(otp);

        try {
            String message = "Your Verification Code is: " + otpValue;
            notificationService.sendEmail(user.getEmail(), "Your OTP Code", message);
            // Log SMS for debugging until production SMS gateway is active
            notificationService.sendSms(user.getPhoneNumber(), message);
        } catch (Exception e) {
            log.error("Notification failed", e);
        }
    }

    private void lockUserAccount(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setStatus(AccountStatus.LOCKED);
            userRepository.save(user);
        });
    }

    private void activateUser(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getStatus() == AccountStatus.PENDING_VERIFICATION) {
                user.setStatus(AccountStatus.ACTIVE);
                userRepository.save(user);
            }
        });
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        userRepository.save(user);
    }
}