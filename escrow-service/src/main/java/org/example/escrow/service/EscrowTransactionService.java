package org.example.escrow.service;

import org.example.escrow.dto.transaction.InitiateTransactionRequest;
import org.example.escrow.dto.transaction.TransactionResponse;

import java.util.List;
import java.util.UUID;

public interface EscrowTransactionService {

    TransactionResponse initiateTransaction(UUID buyerId, InitiateTransactionRequest request);

    TransactionResponse payTransaction(UUID buyerId, UUID transactionId);

    List<TransactionResponse> getTransactionHistory(String email);
}