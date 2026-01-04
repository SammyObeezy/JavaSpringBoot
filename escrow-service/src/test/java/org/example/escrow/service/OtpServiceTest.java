package org.example.escrow.service;

import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.model.OtpCode;
import org.example.escrow.model.User;
import org.example.escrow.repository.OtpCodeRepository;
import org.example.escrow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock private OtpCodeRepository otpCodeRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private OtpService otpService;

    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().email("otp@test.com").phoneNumber("254700000000").build();
        user.setId(userId);

        ReflectionTestUtils.setField(otpService, "otpExpiryMinutes", 5);
        ReflectionTestUtils.setField(otpService, "otpLength", 6);
    }

    @Test
    void generateAndSendOtp_ShouldSaveAndSend() {
        otpService.generateAndSendOtp(user);

        verify(otpCodeRepository, times(1)).save(any(OtpCode.class));
        verify(notificationService, times(1)).sendSms(eq("254700000000"), anyString());
    }

    @Test
    void verifyOtp_ShouldSuccess_WhenValid() {
        // Arrange
        String code = "123456";
        OtpCode validOtp = OtpCode.builder()
                .code(code)
                .user(user)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByUserIdAndCodeAndUsedFalse(userId, code)).thenReturn(Optional.of(validOtp));

        // Act
        otpService.verifyOtp(user.getEmail(), code);

        // Assert
        assertTrue(validOtp.isUsed());
        assertTrue(user.isPhoneVerified());
        verify(otpCodeRepository).save(validOtp);
        verify(userRepository).save(user);
    }

    @Test
    void verifyOtp_ShouldThrow_WhenExpired() {
        // Arrange
        String code = "123456";
        OtpCode expiredOtp = OtpCode.builder()
                .code(code)
                .user(user)
                .used(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1)) // Expired
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(otpCodeRepository.findByUserIdAndCodeAndUsedFalse(userId, code)).thenReturn(Optional.of(expiredOtp));

        // Act & Assert
        assertThrows(BusinessLogicException.class, () -> otpService.verifyOtp(user.getEmail(), code));

        // Ensure we didn't mark it as used or verify the user
        assertFalse(expiredOtp.isUsed());
        assertFalse(user.isPhoneVerified());
    }
}