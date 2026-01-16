package org.example.escrow.service;

import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.model.LedgerEntry;
import org.example.escrow.model.Wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WalletService {

    Wallet depositFunds(UUID userId, DepositRequest request);

    void deductFunds(UUID userId, BigDecimal amount, String currency, String description);

    List<LedgerEntry> getWalletHistory(UUID userId);
}
