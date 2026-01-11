package org.example.escrow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.escrow.dto.identity.LoginRequest;
import org.example.escrow.dto.identity.RegisterRequest;
import org.example.escrow.dto.identity.ResendOtpRequest;
import org.example.escrow.dto.identity.VerifyOtpRequest;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.NotificationChannel;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.OtpCodeRepository;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.repository.WalletRepository;
import org.example.escrow.service.EmailService;
import org.example.escrow.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private OtpCodeRepository otpCodeRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private ObjectMapper objectMapper;

    // We mock the SMS service so we don't need real AWS credentials for tests
    @MockBean private NotificationService notificationService;

    // Mock Email Service to verify email interactions
    @MockBean private EmailService emailService;

    @AfterEach
    void tearDown() {
        // Must delete child records first to satisfy Foreign Key constraints
        // Wallets and OTPs depend on Users, so delete them first
        otpCodeRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email("test.integration@escrow.com")
                .phoneNumber("0700123456")
                .password("StrongPass123#")
                .role(UserRole.ROLE_USER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test.integration@escrow.com"));
    }

    @Test
    void shouldFailRegistration_WhenPhoneInvalid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("Test")
                .lastName("User")
                .email("bad.phone@escrow.com")
                .phoneNumber("123") // Invalid phone
                .password("StrongPass123#")
                .role(UserRole.ROLE_USER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRegister_ResendOtpViaEmail_AndVerify_AndLogin() throws Exception {
        String email = "email.flow@escrow.com";

        // 1. Register (Triggers SMS default)
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstName("Email")
                .lastName("Flow")
                .email(email)
                .phoneNumber("254711223344")
                .password("StrongPass123#")
                .role(UserRole.ROLE_USER)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // 2. Resend OTP via EMAIL
        ResendOtpRequest resendRequest = new ResendOtpRequest();
        resendRequest.setEmail(email);
        resendRequest.setChannel(NotificationChannel.EMAIL);

        mockMvc.perform(post("/api/v1/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resendRequest)))
                .andExpect(status().isOk());

        // Verify Mock Email Service was called
        verify(emailService).sendEmail(eq(email), anyString(), anyString());

        // 3. Retrieve the Code from Database (Since we mocked the sender)
        User user = userRepository.findByEmail(email).orElseThrow();
        var codes = otpCodeRepository.findByUserIdAndUsedFalse(user.getId());
        assertEquals(1, codes.size(), "Should have exactly one active OTP");
        String otpCode = codes.get(0).getCode();

        // 4. Verify Account using the Code
        VerifyOtpRequest verifyRequest = new VerifyOtpRequest();
        verifyRequest.setEmail(email);
        verifyRequest.setCode(otpCode);

        mockMvc.perform(post("/api/v1/auth/verify-phone")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk());

        // 5. Login (Should succeed now that verified is true)
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword("StrongPass123#");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isAccepted()); // 202 Accepted (OTP sent for 2FA)
    }
}