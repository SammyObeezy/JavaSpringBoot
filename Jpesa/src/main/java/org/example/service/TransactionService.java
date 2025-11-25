package org.example.service;

import org.example.config.DatabaseConfig;
import org.example.dto.SendMoneyRequest;
import org.example.dto.TransactionRequest;
import org.example.model.*;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.example.util.InputValidator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    private static final BigDecimal TRANSACTION_FEE = new BigDecimal("5.00");

    // CONFIGURATION: We use the Phone Number as the constant "Business Key"
    private static final String REVENUE_ACCOUNT_PHONE = "000000";

    // 1. Default Constructor (Used by the App)
    public TransactionService() {
        this(new UserRepository(), new WalletRepository(), new TransactionRepository());
    }

    public TransactionService(UserRepository userRepository, WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    public Transaction deposit(TransactionRequest request) {
        User user = validateAndGetUser(request.getPhoneNumber());
        Wallet wallet = getWallet(user.getUserId());
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Positive amount required");

        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        walletRepository.updateBalance(wallet.getWalletId(), newBalance);
        Transaction txn = new Transaction(wallet.getWalletId(), TransactionType.DEPOSIT, request.getAmount(), generateReferenceCode());
        return transactionRepository.save(txn);
    }

    public Transaction buyAirtime(TransactionRequest request) {
        User user = validateAndGetUser(request.getPhoneNumber());
        Wallet wallet = getWallet(user.getUserId());
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Positive amount required");
        if (wallet.getBalance().compareTo(request.getAmount()) < 0) throw new IllegalStateException("Insufficient funds");

        BigDecimal newBalance = wallet.getBalance().subtract(request.getAmount());
        walletRepository.updateBalance(wallet.getWalletId(), newBalance);
        Transaction txn = new Transaction(wallet.getWalletId(), TransactionType.AIRTIME_PURCHASE, request.getAmount(), generateReferenceCode());
        return transactionRepository.save(txn);
    }

    public List<Transaction> getMiniStatement(String phoneNumber) {
        User user = validateAndGetUser(phoneNumber);
        Wallet wallet = getWallet(user.getUserId());
        return transactionRepository.findMiniStatement(wallet.getWalletId());
    }

    /**
     * P2P Transfer with Fee Deduction
     */
    public void sendMoney(SendMoneyRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (request.getSenderPhone().equals(request.getRecipientPhone())) {
            throw new IllegalArgumentException("Cannot send money to yourself");
        }

        User sender = validateAndGetUser(request.getSenderPhone());
        User recipient = validateAndGetUser(request.getRecipientPhone());

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // START TRANSACTION

            try {
                // 1. Get Wallets
                Wallet senderWallet = getWallet(sender.getUserId());
                Wallet recipientWallet = getWallet(recipient.getUserId());

                // 2. Get System Revenue Wallet (Dynamically)
                Wallet systemWallet = getSystemRevenueWallet();

                BigDecimal transferAmount = request.getAmount();
                BigDecimal totalDeduction = transferAmount.add(TRANSACTION_FEE);

                // 3. Check Balance
                if (senderWallet.getBalance().compareTo(totalDeduction) < 0) {
                    throw new IllegalStateException("Insufficient funds. Needed: " + totalDeduction);
                }

                // 4. Update Balances
                walletRepository.updateBalance(conn, senderWallet.getWalletId(), senderWallet.getBalance().subtract(totalDeduction));
                walletRepository.updateBalance(conn, recipientWallet.getWalletId(), recipientWallet.getBalance().add(transferAmount));
                walletRepository.updateBalance(conn, systemWallet.getWalletId(), systemWallet.getBalance().add(TRANSACTION_FEE));

                // 5. Generate Receipts
                String refCode = generateReferenceCode();

                transactionRepository.save(conn, new Transaction(senderWallet.getWalletId(), TransactionType.TRANSFER, transferAmount.negate(), refCode + "-OUT"));
                transactionRepository.save(conn, new Transaction(senderWallet.getWalletId(), TransactionType.TRANSACTION_FEE, TRANSACTION_FEE.negate(), refCode + "-FEE"));
                transactionRepository.save(conn, new Transaction(recipientWallet.getWalletId(), TransactionType.TRANSFER, transferAmount, refCode + "-IN"));
                transactionRepository.save(conn, new Transaction(systemWallet.getWalletId(), TransactionType.TRANSACTION_FEE, TRANSACTION_FEE, refCode + "-REV"));

                conn.commit(); // COMMIT
                System.out.println("Transfer Complete. Fee: " + TRANSACTION_FEE);

            } catch (Exception e) {
                conn.rollback(); // ROLLBACK
                throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // --- Helper to find the Revenue Wallet by Phone ---
    private Wallet getSystemRevenueWallet() {
        User systemUser = userRepository.findByPhoneNumber(REVENUE_ACCOUNT_PHONE)
                .orElseThrow(() -> new IllegalStateException("System Revenue Account (" + REVENUE_ACCOUNT_PHONE + ") not found. Please run DB setup script."));

        return walletRepository.findByUserId(systemUser.getUserId())
                .orElseThrow(() -> new IllegalStateException("System Revenue Wallet missing"));
    }

    // ... Other Helpers ...
    private User validateAndGetUser(String phoneNumber) {
        String normalized = InputValidator.formatPhoneNumber(phoneNumber);
        return userRepository.findByPhoneNumber(normalized)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));
    }

    private String generateReferenceCode() {
        return "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}