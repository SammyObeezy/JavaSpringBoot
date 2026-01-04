package org.example.escrow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.escrow.dto.transaction.InitiateTransactionRequest;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;
import org.example.escrow.model.User;
import org.example.escrow.model.Wallet;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.EscrowTransactionRepository;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.MerchantServiceRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class EscrowControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private MerchantProfileRepository merchantProfileRepository;
    @Autowired private MerchantServiceRepository merchantServiceRepository;
    @Autowired private EscrowTransactionRepository escrowTransactionRepository; // Added for cleanup
    @Autowired private WalletRepository walletRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;

    // Mock notification service to avoid AWS calls during tests
    @MockBean private org.example.escrow.service.NotificationService notificationService;

    private User buyer;
    private User merchantUser;
    private MerchantService service;
    private String buyerToken;

    @BeforeEach
    void setupData() {
        // 1. Create Merchant
        merchantUser = userRepository.save(User.builder()
                .firstName("John").lastName("Wick").email("merchant@test.com")
                .phoneNumber("254700000001").passwordHash("hash").role(UserRole.ROLE_MERCHANT)
                .phoneVerified(true).build());

        MerchantProfile profile = merchantProfileRepository.save(MerchantProfile.builder()
                .user(merchantUser).businessName("Wick Services").commissionRate(BigDecimal.valueOf(0.05)).build());

        service = merchantServiceRepository.save(MerchantService.builder()
                .merchant(profile).name("Security").price(new BigDecimal("1000.00"))
                .currency("KES").active(true).build());

        // 2. Create Buyer
        buyer = userRepository.save(User.builder()
                .firstName("Alice").lastName("Wonder").email("buyer@test.com")
                .phoneNumber("254700000002").passwordHash("hash").role(UserRole.ROLE_USER)
                .phoneVerified(true).build());

        // 3. Create Buyer Wallet with funds
        walletRepository.save(Wallet.builder()
                .user(buyer).balance(new BigDecimal("5000.00")).currency("KES").build());

        // 4. Generate Token for Buyer
        buyerToken = "Bearer " + jwtService.generateToken(buyer);
    }

    @AfterEach
    void cleanUp() {
        // Fix: Delete transactions first to satisfy Foreign Key constraints
        escrowTransactionRepository.deleteAll();
        merchantServiceRepository.deleteAll();
        merchantProfileRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void initiateTransaction_ShouldReturnCreated() throws Exception {
        InitiateTransactionRequest request = new InitiateTransactionRequest();
        request.setServiceId(service.getId());

        mockMvc.perform(post("/api/v1/transactions/initiate")
                        .header("Authorization", buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("CREATED"))
                // Fixed expectation: 1000 + 50% fee (from app.properties 0.50) = 1500.00
                .andExpect(jsonPath("$.data.totalAmount").value(1500.00));
    }
}