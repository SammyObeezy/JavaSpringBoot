package org.example.escrow.service;

import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.identity.AuthResponse;
import org.example.escrow.dto.identity.RegisterRequest;
import org.example.escrow.dto.mapper.UserMapper;
import org.example.escrow.exception.DuplicateResourceException;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AppProperties appProperties;
    @Mock private OtpService otpService;
    // We need to mock the Config inner class structure for AppProperties
    @Mock private AppProperties.Escrow escrowProps;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "John", "Doe", "john@test.com", "0712345678", "Pass123#", UserRole.ROLE_USER
        );

        // Fixed: Removed .id() from builder because @Builder doesn't see parent fields.
        // We set ID manually using the setter below.
        user = User.builder()
                .email("john@test.com")
                .phoneNumber("254712345678")
                .passwordHash("encoded_hash")
                .build();
        user.setId(java.util.UUID.randomUUID());

        // Mock AppProperties behavior
        lenient().when(appProperties.getEscrow()).thenReturn(escrowProps);
        lenient().when(escrowProps.getDefaultCurrency()).thenReturn("KES");
    }

    @Test
    void register_ShouldRegisterUser_WhenDataIsValid() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(RegisterRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_hash");
        when(userRepository.save(any(User.class))).thenReturn(user);

        AuthResponse mockResponse = AuthResponse.builder().email("john@test.com").build();
        when(userMapper.toAuthResponse(any(User.class))).thenReturn(mockResponse);

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("john@test.com", response.getEmail());

        // Verify interactions
        verify(walletRepository, times(1)).save(any()); // Wallet created?
        verify(otpService, times(1)).generateAndSendOtp(any()); // OTP sent?
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> authService.register(registerRequest));

        // Verify no saves happened
        verify(userRepository, never()).save(any());
        verify(otpService, never()).generateAndSendOtp(any());
    }
}