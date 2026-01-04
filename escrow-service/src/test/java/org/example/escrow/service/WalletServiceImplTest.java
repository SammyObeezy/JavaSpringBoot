package org.example.escrow.service;

import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.model.User;
import org.example.escrow.model.Wallet;
import org.example.escrow.repository.LedgerEntryRepository;
import org.example.escrow.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock private WalletRepository walletRepository;
    @Mock private LedgerEntryRepository ledgerEntryRepository;
    @Mock private AppProperties appProperties;
    @Mock private AppProperties.Escrow escrowProps;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User user;
    private Wallet wallet;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Fixed: Set ID manually because @Builder doesn't see BaseEntity fields
        user = User.builder()
                .email("test@escrow.com")
                .firstName("Test")
                .lastName("User")
                .build();
        user.setId(userId);

        // Fixed: Set ID manually
        wallet = Wallet.builder()
                .user(user)
                .balance(new BigDecimal("1000.00"))
                .currency("KES")
                .build();
        wallet.setId(UUID.randomUUID());

        // Lenient allows stubbing even if not called in every single test method
        lenient().when(appProperties.getEscrow()).thenReturn(escrowProps);
        lenient().when(escrowProps.getDefaultCurrency()).thenReturn("KES");
    }

    @Test
    void depositFunds_ShouldIncreaseBalance_WhenSuccessful() {
        // Arrange
        DepositRequest request = new DepositRequest();
        request.setAmount(new BigDecimal("500.00"));
        request.setCurrency("KES");

        when(walletRepository.findByUserIdAndCurrency(userId, "KES")).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Wallet result = walletService.depositFunds(userId, request);

        // Assert
        assertEquals(new BigDecimal("1500.00"), result.getBalance()); // 1000 + 500
        verify(ledgerEntryRepository, times(1)).save(any()); // Ensure audit log is created
    }

    @Test
    void deductFunds_ShouldDecreaseBalance_WhenEnoughFunds() {
        // Arrange
        BigDecimal deduction = new BigDecimal("200.00");
        when(walletRepository.findByUserIdAndCurrency(userId, "KES")).thenReturn(Optional.of(wallet));

        // Act
        walletService.deductFunds(userId, deduction, "KES", "Payment");

        // Assert
        assertEquals(new BigDecimal("800.00"), wallet.getBalance()); // 1000 - 200
        verify(ledgerEntryRepository, times(1)).save(any());
    }

    @Test
    void deductFunds_ShouldThrowException_WhenInsufficientFunds() {
        // Arrange
        BigDecimal largeDeduction = new BigDecimal("5000.00"); // More than 1000 balance
        when(walletRepository.findByUserIdAndCurrency(userId, "KES")).thenReturn(Optional.of(wallet));

        // Act & Assert
        assertThrows(BusinessLogicException.class, () ->
                walletService.deductFunds(userId, largeDeduction, "KES", "Payment")
        );

        // Verify balance didn't change and nothing was saved
        assertEquals(new BigDecimal("1000.00"), wallet.getBalance());
        verify(walletRepository, never()).save(any());
    }
}