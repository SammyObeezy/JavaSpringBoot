package org.example.escrow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.model.User;
import org.example.escrow.model.Wallet;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.LedgerEntryRepository;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.repository.WalletRepository;
import org.example.escrow.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WalletControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private LedgerEntryRepository ledgerEntryRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;

    // Mock notification service to avoid AWS calls during tests
    @MockBean private org.example.escrow.service.NotificationService notificationService;

    private User user;
    private String token;

    @BeforeEach
    void setupData() {
        // 1. Create User
        user = userRepository.save(User.builder()
                .firstName("Richie").lastName("Rich").email("wallet@test.com")
                .phoneNumber("254700000099").passwordHash("hash").role(UserRole.ROLE_USER)
                .phoneVerified(true).build());

        // 2. Create Initial Wallet (Balance 0)
        walletRepository.save(Wallet.builder()
                .user(user).balance(BigDecimal.ZERO).currency("KES").build());

        // 3. Generate Token
        token = "Bearer " + jwtService.generateToken(user);
    }

    @AfterEach
    void cleanUp() {
        ledgerEntryRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void deposit_ShouldIncreaseBalance_WhenRequestIsValid() throws Exception {
        // Arrange
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("5000.00"));
        request.setCurrency("KES");

        // Act
        mockMvc.perform(post("/api/v1/wallets/deposit")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(5000.00))
                .andExpect(jsonPath("$.data.ownerName").value("Richie Rich"));

        // Assert DB State
        Wallet wallet = walletRepository.findByUserIdAndCurrency(user.getId(), "KES").orElseThrow();
        assertEquals(0, new BigDecimal("5000.00").compareTo(wallet.getBalance()));
    }

    @Test
    void deposit_ShouldFail_WhenAmountIsInvalid() throws Exception {
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("-100.00")); // Negative amount
        request.setCurrency("KES");

        mockMvc.perform(post("/api/v1/wallets/deposit")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Validation error
    }
}