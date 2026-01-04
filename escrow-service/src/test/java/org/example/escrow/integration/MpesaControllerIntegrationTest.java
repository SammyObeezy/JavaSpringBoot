package org.example.escrow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.escrow.dto.mpesa.MpesaDto;
import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.service.JwtService;
import org.example.escrow.service.MpesaService;
import org.example.escrow.service.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class MpesaControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private ObjectMapper objectMapper;

    // Mock the logic layer (we tested this in MpesaServiceTest already)
    @MockBean private MpesaService mpesaService;
    @MockBean private WalletServiceImpl walletService;
    @MockBean private org.example.escrow.service.NotificationService notificationService;

    private User user;
    private String token;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        user = userRepository.save(User.builder()
                .firstName("Mpesa").lastName("User").email("mpesa@test.com")
                .phoneNumber("254799887766").passwordHash("hash").role(UserRole.ROLE_USER)
                .phoneVerified(true).build());

        token = "Bearer " + jwtService.generateToken(user);
    }

    @Test
    void triggerDeposit_ShouldCallService_AndReturnOk() throws Exception {
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setCurrency("KES");

        MpesaDto.StkPushSyncResponse mockResponse = new MpesaDto.StkPushSyncResponse();
        mockResponse.setResponseCode("0");
        mockResponse.setCustomerMessage("Success");

        // Use any() for the ID string since UUIDs change
        when(mpesaService.initiateStkPush(any(User.class), eq(BigDecimal.valueOf(100)), any(String.class)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/mpesa/deposit")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify service was called with correct amount
        verify(mpesaService).initiateStkPush(any(User.class), eq(BigDecimal.valueOf(100)), any(String.class));
    }

    @Test
    void handleCallback_ShouldProcessAndReturnOk() throws Exception {
        // Construct a minimal Callback JSON
        String callbackJson = """
            {
                "Body": {
                    "stkCallback": {
                        "MerchantRequestID": "123",
                        "CheckoutRequestID": "xyz",
                        "ResultCode": 0,
                        "ResultDesc": "Success"
                    }
                }
            }
            """;

        mockMvc.perform(post("/api/v1/mpesa/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(callbackJson))
                .andExpect(status().isOk());

        // Verify the controller handed off the logic to the service
        verify(mpesaService).processCallback(any(MpesaDto.StkCallbackRequest.StkCallback.class));
    }
}