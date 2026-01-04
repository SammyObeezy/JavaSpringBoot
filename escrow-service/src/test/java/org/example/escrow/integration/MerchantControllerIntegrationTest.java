package org.example.escrow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.escrow.dto.merchant.CreateServiceRequest;
import org.example.escrow.dto.merchant.MerchantOnboardingRequest;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.MerchantServiceRepository;
import org.example.escrow.repository.UserRepository;
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
class MerchantControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private MerchantProfileRepository merchantProfileRepository;
    @Autowired private MerchantServiceRepository merchantServiceRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;

    @MockBean private org.example.escrow.service.NotificationService notificationService;

    private User user;
    private String token;

    @BeforeEach
    void setupData() {
        // Create a standard user
        user = userRepository.save(User.builder()
                .firstName("John").lastName("Trader").email("trader@test.com")
                .phoneNumber("254711111111").passwordHash("hash").role(UserRole.ROLE_USER)
                .phoneVerified(true).build());

        token = "Bearer " + jwtService.generateToken(user);
    }

    @AfterEach
    void cleanUp() {
        merchantServiceRepository.deleteAll();
        merchantProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void onboardMerchant_ShouldUpgradeRoleAndCreateProfile() throws Exception {
        MerchantOnboardingRequest request = new MerchantOnboardingRequest("John's Shop", "REG-001");

        mockMvc.perform(post("/api/v1/merchants/onboard")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.businessName").value("John's Shop"));

        // Verify DB updates
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(UserRole.ROLE_MERCHANT, updatedUser.getRole()); // Role Changed?

        assertEquals(1, merchantProfileRepository.count()); // Profile Created?
    }

    @Test
    void createService_ShouldSucceed_WhenUserIsMerchant() throws Exception {
        // 1. Manually make user a merchant first (Pre-requisite)
        merchantProfileRepository.save(MerchantProfile.builder()
                .user(user).businessName("Existing Shop").build());

        user.setRole(UserRole.ROLE_MERCHANT);
        userRepository.save(user);

        // 2. Try to add a service
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Consulting");
        request.setPrice(new BigDecimal("1000.00"));
        request.setDescription("Expert advice");

        mockMvc.perform(post("/api/v1/merchants/services")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Consulting"));

        assertEquals(1, merchantServiceRepository.count());
    }
}