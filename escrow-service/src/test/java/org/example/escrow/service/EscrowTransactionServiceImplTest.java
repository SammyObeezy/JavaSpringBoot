package org.example.escrow.service;

import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.mapper.EscrowMapper;
import org.example.escrow.dto.transaction.InitiateTransactionRequest;
import org.example.escrow.model.EscrowTransaction;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;
import org.example.escrow.model.User;
import org.example.escrow.repository.EscrowTransactionRepository;
import org.example.escrow.repository.MerchantServiceRepository;
import org.example.escrow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscrowTransactionServiceImplTest {

    @Mock private EscrowTransactionRepository transactionRepository;
    @Mock private MerchantServiceRepository merchantServiceRepository;
    @Mock private UserRepository userRepository;
    @Mock private AppProperties appProperties;
    @Mock private AppProperties.Escrow escrowProps;
    @Mock private EscrowMapper escrowMapper;
    @Mock private WalletServiceImpl walletService;

    @InjectMocks
    private EscrowTransactionServiceImpl escrowService;

    private User buyer;
    private User merchantUser;
    private MerchantService itemService;
    private UUID serviceId = UUID.randomUUID();
    private UUID buyerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        buyer = User.builder().email("buyer@test.com").build();
        buyer.setId(buyerId);

        merchantUser = User.builder().email("seller@test.com").build();
        merchantUser.setId(UUID.randomUUID());

        MerchantProfile profile = MerchantProfile.builder().user(merchantUser).build();
        profile.setId(UUID.randomUUID()); // Fix: ID required for some logic

        // Fixed: Use setter for ID instead of builder
        itemService = MerchantService.builder()
                .merchant(profile)
                .price(new BigDecimal("1000.00"))
                .currency("KES")
                .active(true)
                .build();
        itemService.setId(serviceId);

        // Config Mock: 5% Fee
        lenient().when(appProperties.getEscrow()).thenReturn(escrowProps);
        lenient().when(escrowProps.getPlatformFeePercentage()).thenReturn(0.05); // 5%
    }

    @Test
    void initiateTransaction_ShouldCalculateTotalsCorrectly() {
        // Arrange
        InitiateTransactionRequest request = new InitiateTransactionRequest();
        request.setServiceId(serviceId);

        when(userRepository.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(merchantServiceRepository.findById(serviceId)).thenReturn(Optional.of(itemService));

        // Mock the save to return the same object passed to it
        when(transactionRepository.save(any(EscrowTransaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        escrowService.initiateTransaction(buyerId, request);

        // Assert
        // We capture the actual entity saved to the DB to verify the math inside it
        ArgumentCaptor<EscrowTransaction> captor = ArgumentCaptor.forClass(EscrowTransaction.class);
        verify(transactionRepository).save(captor.capture());
        EscrowTransaction savedTx = captor.getValue();

        // Calculations:
        // Price = 1000.00
        // Fee = 5% = 50.00
        // Total = 1050.00

        // Use compareTo for safe BigDecimal comparison (ignores 50.0 vs 50.00 scale differences)
        assertEquals(0, new BigDecimal("50.00").compareTo(savedTx.getPlatformFee()), "Platform fee should be 50.00");
        assertEquals(0, new BigDecimal("1050.00").compareTo(savedTx.getTotalAmount()), "Total amount should be 1050.00");
    }
}