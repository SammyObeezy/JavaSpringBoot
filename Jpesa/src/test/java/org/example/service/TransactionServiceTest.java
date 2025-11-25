package org.example.service;

import org.example.dto.TransactionRequest;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.model.User;
import org.example.model.Wallet;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(userRepository, walletRepository, transactionRepository);
    }

    @Test
    void shouldDepositSuccessfully() {
        // --- ARRANGE ---
        String phone = "254700000001";
        BigDecimal depositAmount = new BigDecimal("1000.00");
        BigDecimal initialBalance = BigDecimal.ZERO;

        // Mock User
        User mockUser = new User();
        mockUser.setUserId(101L);
        mockUser.setPhoneNumber(phone);
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(mockUser));

        // Mock Wallet
        Wallet mockWallet = new Wallet(101L);
        mockWallet.setWalletId(501L);
        mockWallet.setBalance(initialBalance);
        when(walletRepository.findByUserId(101L)).thenReturn(Optional.of(mockWallet));

        // Mock Transaction Save (return the object passed to it)
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // --- ACT ---
        TransactionRequest request = new TransactionRequest("0700000001", depositAmount);
        Transaction result = transactionService.deposit(request);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals(TransactionType.DEPOSIT, result.getTxnType());
        assertEquals(depositAmount, result.getAmount());

        // Verify balance update: 0 + 1000 = 1000
        verify(walletRepository).updateBalance(eq(501L), eq(new BigDecimal("1000.00")));
    }

    @Test
    void shouldBuyAirtimeSuccessfully() {
        // --- ARRANGE ---
        String phone = "254700000002";
        BigDecimal airtimeAmount = new BigDecimal("50.00");
        BigDecimal initialBalance = new BigDecimal("200.00");

        User mockUser = new User();
        mockUser.setUserId(102L);
        mockUser.setPhoneNumber(phone);
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(mockUser));

        Wallet mockWallet = new Wallet(102L);
        mockWallet.setWalletId(502L);
        mockWallet.setBalance(initialBalance);
        when(walletRepository.findByUserId(102L)).thenReturn(Optional.of(mockWallet));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // --- ACT ---
        TransactionRequest request = new TransactionRequest("0700000002", airtimeAmount);
        Transaction result = transactionService.buyAirtime(request);

        // --- ASSERT ---
        assertNotNull(result);
        assertEquals(TransactionType.AIRTIME_PURCHASE, result.getTxnType());

        // Verify balance update: 200 - 50 = 150
        verify(walletRepository).updateBalance(eq(502L), eq(new BigDecimal("150.00")));
    }

    @Test
    void shouldFailAirtimeIfInsufficientFunds() {
        // --- ARRANGE ---
        String phone = "254700000003";
        BigDecimal airtimeAmount = new BigDecimal("500.00");
        BigDecimal initialBalance = new BigDecimal("50.00"); // Too poor

        User mockUser = new User();
        mockUser.setUserId(103L);
        mockUser.setPhoneNumber(phone);
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(mockUser));

        Wallet mockWallet = new Wallet(103L);
        mockWallet.setWalletId(503L);
        mockWallet.setBalance(initialBalance);
        when(walletRepository.findByUserId(103L)).thenReturn(Optional.of(mockWallet));

        // --- ACT & ASSERT ---
        TransactionRequest request = new TransactionRequest("0700000003", airtimeAmount);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            transactionService.buyAirtime(request);
        });

        assertEquals("Insufficient funds", ex.getMessage());

        // Ensure NO balance update happened
        verify(walletRepository, never()).updateBalance(anyLong(), any());
    }

    @Test
    void shouldReturnMiniStatement() {
        // --- ARRANGE ---
        String phone = "254700000004";
        User mockUser = new User();
        mockUser.setUserId(104L);
        mockUser.setPhoneNumber(phone);
        when(userRepository.findByPhoneNumber(phone)).thenReturn(Optional.of(mockUser));

        Wallet mockWallet = new Wallet(104L);
        mockWallet.setWalletId(504L);
        when(walletRepository.findByUserId(104L)).thenReturn(Optional.of(mockWallet));

        // Mock Repo returning a list of 2 transactions
        when(transactionRepository.findMiniStatement(504L)).thenReturn(List.of(new Transaction(), new Transaction()));

        // --- ACT ---
        List<Transaction> result = transactionService.getMiniStatement("0700000004");

        // --- ASSERT ---
        assertEquals(2, result.size());
    }
}