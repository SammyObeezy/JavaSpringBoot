package org.example.escrow.service;

import org.example.escrow.dto.mpesa.MpesaDto;
import org.example.escrow.integration.MpesaClient;
import org.example.escrow.model.User;
import org.example.escrow.repository.MpesaTransactionRepository;
import org.example.escrow.service.MpesaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MpesaServiceTest {

    @Mock private MpesaClient mpesaClient; // Mock the Integration Layer
    @Mock private MpesaTransactionRepository mpesaTransactionRepository;
    @Mock private WalletService walletService; // Use Interface

    @InjectMocks
    private MpesaServiceImpl mpesaService; // Test the Implementation

    @Test
    void initiateStkPush_ShouldCallClientAndSaveTransaction_WhenSuccessful() {
        // Arrange
        // We set ID manually as BaseEntity fields aren't in @Builder
        User user = User.builder().phoneNumber("254712345678").build();
        user.setId(UUID.randomUUID());

        BigDecimal amount = BigDecimal.valueOf(100);
        String reference = "Ref";

        // Mock the response from the Client layer
        MpesaDto.StkPushSyncResponse mockResponse = new MpesaDto.StkPushSyncResponse();
        mockResponse.setResponseCode("0");
        mockResponse.setCheckoutRequestId("xyz");
        mockResponse.setMerchantRequestId("abc");
        mockResponse.setCustomerMessage("Success");

        // Define behavior
        when(mpesaClient.performStkPush(eq("254712345678"), eq(amount), eq(reference)))
                .thenReturn(mockResponse);

        // Act
        MpesaDto.StkPushSyncResponse result = mpesaService.initiateStkPush(user, amount, reference);

        // Assert
        assertNotNull(result);
        assertEquals("0", result.getResponseCode());
        assertEquals("xyz", result.getCheckoutRequestId());

        // Verify that the Service delegated to the Client correctly
        verify(mpesaClient).performStkPush(eq("254712345678"), eq(amount), eq(reference));

        // Verify that the Service persisted the transaction state (Business Logic)
        verify(mpesaTransactionRepository).save(any());
    }
}