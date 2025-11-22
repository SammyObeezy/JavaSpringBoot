package org.example.service;

import org.example.dto.TransactionRequest;
import org.example.model.Transaction;
import org.example.model.TransactionType;
import org.example.model.User;
import org.example.model.Wallet;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.example.util.InputValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService() {
        this.userRepository = new UserRepository();
        this.walletRepository = new WalletRepository();
        this.transactionRepository = new TransactionRepository();
    }

    /**
     * Process a Deposit
     */
    public Transaction deposit(TransactionRequest request){
        // 1. Validate User & Get Wallet
        User user = validateAndGetUser(request.getPhoneNumber());
        Wallet wallet = getWallet(user.getUserId());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        // 2. Update Balance (Add)
        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        walletRepository.updateBalance(wallet.getWalletId(), newBalance);

        // 3. Create & Save Receipt
        Transaction txn = new Transaction(
                wallet.getWalletId(),
                TransactionType.DEPOSIT,
                request.getAmount(),
                generateReferenceCode()
        );
        return transactionRepository.save(txn);
    }

    /**
     * Process Airtime Purchase.
     */
    public Transaction buyAirtime(TransactionRequest request){
        User user = validateAndGetUser(request.getPhoneNumber());
        Wallet wallet = getWallet(user.getUserId());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <=0){
            throw new IllegalArgumentException("Airtime amount must be positive");
        }

        // 1. Check for Insufficient Funds
        if (wallet.getBalance().compareTo(request.getAmount()) < 0){
            throw new IllegalStateException("Insufficient funds. Balance: " + wallet.getBalance());
        }

        // 2. Deduct Money
        BigDecimal newBalance = wallet.getBalance().subtract(request.getAmount());
        walletRepository.updateBalance(wallet.getWalletId(), newBalance);

        // 3. Create & Save Receipt
        Transaction txn = new Transaction(
                wallet.getWalletId(),
                TransactionType.AIRTIME_PURCHASE,
                request.getAmount(),
                generateReferenceCode()
        );

        return transactionRepository.save(txn);
    }
    /**
    *Get Mini Statement (Last 10 Txns)
     */
    public List<Transaction> getMiniStatement(String phoneNumber){
        User user = validateAndGetUser(phoneNumber);
        Wallet wallet = getWallet(user.getUserId());
        return transactionRepository.findMiniStatement(wallet.getWalletId());
    }

    // Helpers
    private User validateAndGetUser(String phoneNumber){
        String normalized = InputValidator.formatPhoneNumber(phoneNumber);
        return userRepository.findByPhoneNumber(normalized)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));
    }

    private Wallet getWallet(Long userId){
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user"));
    }

    private String generateReferenceCode() {
        // Generated a code like "TXT-ABCD1234"
        return "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
