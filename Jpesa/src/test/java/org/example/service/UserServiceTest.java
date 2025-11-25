package org.example.service;

import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.model.User;
import org.example.model.Wallet;
import org.example.repository.OtpRepository;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private OtpRepository otpRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Manually inject the mocks via the new constructor
        userService = new UserService(userRepository, walletRepository, otpRepository);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // --- ARRANGE ---
        // Unique Phone Number 1
        String rawPhoneNumber = "0711223344";
        String normalizedPhone = "254711223344";

        RegisterRequest request = new RegisterRequest(
                "Test User One", rawPhoneNumber, "user1@example.com", "StrongPass1!"
        );

        // Mock: When checking if user exists, return Empty (User is new)
        // Ensure this matches 'normalizedPhone' exactly
        when(userRepository.findByPhoneNumber(normalizedPhone)).thenReturn(Optional.empty());

        // Mock: When saving, return a dummy user
        User savedUserStub = new User("Test User One", normalizedPhone, "user1@example.com", "hashed_pass");
        savedUserStub.setUserId(10L);

        // Use 'any(User.class)' here to be flexible about the exact User object passed to save
        when(userRepository.save(any(User.class))).thenReturn(savedUserStub);

        // --- ACT ---
        User result = userService.registerUser(request);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals(normalizedPhone, result.getPhoneNumber());

        // Verify wallet creation was attempted
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void shouldFailLoginWithWrongPassword() {
        // --- ARRANGE ---
        // Unique Phone Number 2 (Different from the registration test)
        String rawPhoneNumber = "0799887766";
        String normalizedPhone = "254799887766";
        String realPassword = "StrongPass1!";
        String wrongPassword = "WrongPass123!";

        // Create a fake user in "DB" with a real BCrypt hash of the correct password
        String dbHash = BCrypt.hashpw(realPassword, BCrypt.gensalt());
        User fakeUser = new User("Test User Two", normalizedPhone, "user2@email.com", dbHash);

        // When finding user, return our fake user
        when(userRepository.findByPhoneNumber(normalizedPhone)).thenReturn(Optional.of(fakeUser));

        // --- ACT & ASSERT ---
        LoginRequest loginRequest = new LoginRequest(rawPhoneNumber, wrongPassword);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.loginUser(loginRequest);
        });

        assertEquals("Invalid phone number or password", exception.getMessage());
    }
}