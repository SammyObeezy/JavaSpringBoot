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
public class WalletServiceImpl {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AppProperties appProperties;

    @Transactional
    public Wallet depositFunds(UUID userId, DepositRequest request) {
        String currency = request.getCurrency() != null ? request.getCurrency() : appProperties.getEscrow().getDefaultCurrency();

        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "user_id/currency", userId + "/" + currency));

        BigDecimal amount = request.getAmount();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // Accessing fields for logging also forces initialization (solving LazyInit issue)
        // and removes the "result ignored" warning because we are using the result.
        if (wallet.getUser() != null) {
            log.info("Deposited funds for user: {} {}", wallet.getUser().getFirstName(), wallet.getUser().getLastName());
        }

        LedgerEntry entry = LedgerEntry.builder()
                .wallet(wallet)
                .transactionId(UUID.randomUUID())
                .amount(amount)
                .entryType(LedgerEntryType.DEPOSIT)
                .description("Manual Deposit via API")
                .build();

        ledgerEntryRepository.save(entry);

        return wallet;
    }

    @Transactional
    public void deductFunds(UUID userId, BigDecimal amount, String currency, String description) {
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "user_id/currency", userId + "/" + currency));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessLogicException("Insufficient funds. Current balance: " + wallet.getBalance());
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        LedgerEntry entry = LedgerEntry.builder()
                .wallet(wallet)
                .transactionId(UUID.randomUUID())
                .amount(amount.negate()) // Negative for withdrawals
                .entryType(LedgerEntryType.WITHDRAWAL)
                .description(description)
                .build();

        ledgerEntryRepository.save(entry);
    }

    // Marked as readOnly to keep session open for any lazy loading in results
    @Transactional(readOnly = true)
    @SuppressWarnings("unused") // Suppressed "unused" warning as this is a public API method for future controller use
    public List<LedgerEntry> getWalletHistory(UUID userId) {
        String currency = appProperties.getEscrow().getDefaultCurrency();
        Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", "user_id", userId));

        return ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }
}