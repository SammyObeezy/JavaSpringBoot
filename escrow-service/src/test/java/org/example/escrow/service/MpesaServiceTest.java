package org.example.escrow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.mpesa.MpesaDto;
import org.example.escrow.model.User;
import org.example.escrow.repository.MpesaTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MpesaServiceTest {

    @Mock private OkHttpClient okHttpClient;
    @Mock private AppProperties appProperties;
    @Mock private MpesaTransactionRepository mpesaTransactionRepository;
    @Mock private WalletServiceImpl walletService;
    @Mock private Call remoteCall;

    @InjectMocks
    private MpesaService mpesaService;

    // We use a real ObjectMapper to verify JSON parsing
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock private AppProperties.Mpesa mpesaProps;

    @BeforeEach
    void setUp() {
        // Setup config mocks
        lenient().when(appProperties.getMpesa()).thenReturn(mpesaProps);
        lenient().when(mpesaProps.getConsumerKey()).thenReturn("key");
        lenient().when(mpesaProps.getConsumerSecret()).thenReturn("secret");
        lenient().when(mpesaProps.getAuthUrl()).thenReturn("http://auth.url");
        lenient().when(mpesaProps.getStkPushUrl()).thenReturn("http://stk.url");
        lenient().when(mpesaProps.getShortcode()).thenReturn("174379");
        lenient().when(mpesaProps.getPasskey()).thenReturn("passkey");
        lenient().when(mpesaProps.getCallbackUrl()).thenReturn("http://callback.url");

        // Use the real ObjectMapper in the service (InjectMocks might not pick it up if not @Mock)
        // Ideally, we constructor inject, but for this test setup we can rely on reflection or manual setting if needed.
        // For simplicity in this example, we assume InjectMocks works or we would manually init.
        mpesaService = new MpesaService(okHttpClient, objectMapper, appProperties, mpesaTransactionRepository, walletService);
    }

    @Test
    void initiateStkPush_ShouldReturnResponse_WhenMpesaAccepts() throws IOException {
        // Arrange
        // Fixed: User.builder() does not expose .id() because it is in the BaseEntity.
        // We build the user first, then set the ID.
        User user = User.builder().phoneNumber("254712345678").build();
        user.setId(UUID.randomUUID());

        // Mock Auth Response
        String authJson = "{\"access_token\": \"valid_token\", \"expires_in\": \"3599\"}";
        Response authResponse = new Response.Builder()
                .request(new Request.Builder().url("http://auth.url").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(authJson, MediaType.get("application/json")))
                .build();

        // Mock STK Response
        String stkJson = "{\"MerchantRequestID\":\"123\",\"CheckoutRequestID\":\"xyz\",\"ResponseCode\":\"0\"}";
        Response stkResponse = new Response.Builder()
                .request(new Request.Builder().url("http://stk.url").build())
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(stkJson, MediaType.get("application/json")))
                .build();

        // Chain the mocks: Client -> NewCall -> Execute
        when(okHttpClient.newCall(any(Request.class)))
                .thenReturn(remoteCall);
        when(remoteCall.execute())
                .thenReturn(authResponse) // First call (Auth)
                .thenReturn(stkResponse); // Second call (STK Push)

        // Act
        MpesaDto.StkPushSyncResponse result = mpesaService.initiateStkPush(user, BigDecimal.TEN, "Ref");

        // Assert
        assertNotNull(result);
        assertEquals("0", result.getResponseCode());
        assertEquals("xyz", result.getCheckoutRequestId());
    }
}