package org.example.escrow.service;

import org.example.escrow.model.User;
import org.example.escrow.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        // Inject values that are usually read from application.properties
        // This is a 256-bit secret key for testing
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMinutes", 60L);

        user = User.builder()
                .email("test@escrow.com")
                .role(UserRole.ROLE_USER)
                .build();
        user.setId(UUID.randomUUID());
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);
        assertEquals("test@escrow.com", username);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForCorrectUser() {
        String token = jwtService.generateToken(user);

        // Mock UserDetails
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@escrow.com")
                .password("password")
                .roles("USER")
                .build();

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForWrongUser() {
        String token = jwtService.generateToken(user);

        UserDetails wrongUser = org.springframework.security.core.userdetails.User
                .withUsername("hacker@escrow.com") // Different email
                .password("password")
                .roles("USER")
                .build();

        assertFalse(jwtService.isTokenValid(token, wrongUser));
    }
}