package org.example.escrow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.LedgerEntry;
import org.example.escrow.model.Wallet;
import org.example.escrow.model.enums.LedgerEntryType;
import org.example.escrow.repository.LedgerEntryRepository;
import org.example.escrow.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public Wallet depositFunds(UUID userId, DepositRequest request) {
        // 1. Fail Fast: Validate Inputs
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessLogicException("Deposit amount must be positive.");
        }

        String currency = request.getCurrency() != null ? request.getCurrency() : appProperties.getEscrow().getDefaultCurrency();

        // 2. Fetch Data
        Wallet wallet = getWalletOrThrow(userId, currency);

        // 3. Execute Logic
        BigDecimal amount = request.getAmount();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // 4. Audit Trail
        // Force initialization for mapper if needed (though DTO pattern avoids this)
        if (wallet.getUser() != null) {
            log.info("Deposited {} {} for user: {}", amount, currency, wallet.getUser().getEmail());
        }

        saveLedgerEntry(wallet, amount, LedgerEntryType.DEPOSIT, "Manual Deposit via API");

        return wallet;
    }

    @Override
    @Transactional
    public void deductFunds(UUID userId, BigDecimal amount, String currency, String description) {
        // 1. Fail Fast
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessLogicException("Deduction amount must be positive.");
        }

        Wallet wallet = getWalletOrThrow(userId, currency);

        // 2. Business Rule Check
        if (wallet.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient Funds: User {} has {} but tried to spend {}", userId, wallet.getBalance(), amount);
            throw new BusinessLogicException("Insufficient funds. Current balance: " + wallet.getBalance());
        }

        // 3. Execute Logic
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // 4. Audit Trail
        saveLedgerEntry(wallet, amount.negate(), LedgerEntryType.WITHDRAWAL, description);
    }

    @Override
    @Transactional(readOnly = true) // Performance Optimization
    public List<LedgerEntry> getWalletHistory(UUID userId) {
        String currency = appProperties.getEscrow().getDefaultCurrency();
        Wallet wallet = getWalletOrThrow(userId, currency);
        return ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    // --- Helper Methods (DRY Principle) ---

    private Wallet getWalletOrThrow(UUID userId, String currency) {
        return walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "user_id/currency", userId + "/" + currency));
    }

    private void saveLedgerEntry(Wallet wallet, BigDecimal amount, LedgerEntryType type, String description) {
        LedgerEntry entry = LedgerEntry.builder()
                .wallet(wallet)
                .transactionId(UUID.randomUUID())
                .amount(amount)
                .entryType(type)
                .description(description)
                .build();
        ledgerEntryRepository.save(entry);
    }
}