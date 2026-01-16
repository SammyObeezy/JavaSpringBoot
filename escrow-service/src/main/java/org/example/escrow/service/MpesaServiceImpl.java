package org.example.escrow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.escrow.dto.mpesa.MpesaDto;
import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.integration.MpesaClient;
import org.example.escrow.model.MpesaTransaction;
import org.example.escrow.model.User;
import org.example.escrow.repository.MpesaTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaServiceImpl implements MpesaService {

    private final MpesaClient mpesaClient; // The Integration Layer
    private final MpesaTransactionRepository mpesaTransactionRepository;
    private final WalletService walletService; // The Wallet Interface

    @Override
    @Transactional
    public MpesaDto.StkPushSyncResponse initiateStkPush(User user, BigDecimal amount, String reference) {
        // 1. Fail Fast
        if (amount.compareTo(BigDecimal.TEN) < 0) {
            throw new BusinessLogicException("Minimum deposit is 10 KES");
        }

        // 2. Call Integration Layer
        MpesaDto.StkPushSyncResponse response = mpesaClient.performStkPush(user.getPhoneNumber(), amount, reference);

        // 3. Persist Pending State
        MpesaTransaction transaction = MpesaTransaction.builder()
                .user(user)
                .amount(amount)
                .merchantRequestId(response.getMerchantRequestId())
                .checkoutRequestId(response.getCheckoutRequestId())
                .status("PENDING")
                .build();

        mpesaTransactionRepository.save(transaction);

        return response;
    }

    @Override
    @Transactional
    public void processCallback(MpesaDto.StkCallbackRequest.StkCallback callbackBody) {
        String checkoutId = callbackBody.getCheckoutRequestId();

        // 1. Lookup & Idempotency
        MpesaTransaction transaction = mpesaTransactionRepository.findByCheckoutRequestId(checkoutId)
                .orElseThrow(() -> new ResourceNotFoundException("MpesaTransaction", "checkoutId", checkoutId));

        if (!"PENDING".equals(transaction.getStatus())) {
            log.warn("Duplicate Callback ignored for ID: {}", checkoutId);
            return;
        }

        // 2. Logic Branching
        if (callbackBody.getResultCode() == 0) {
            handleSuccess(transaction, callbackBody);
        } else {
            handleFailure(transaction, callbackBody);
        }
    }

    private void handleSuccess(MpesaTransaction tx, MpesaDto.StkCallbackRequest.StkCallback body) {
        tx.setStatus("COMPLETED");

        String receipt = body.getCallbackMetadata().getItem().stream()
                .filter(i -> "MpesaReceiptNumber".equals(i.getName()))
                .findFirst()
                .map(i -> i.getValue().toString())
                .orElse("UNKNOWN");

        tx.setMpesaReceiptNumber(receipt);
        mpesaTransactionRepository.save(tx);

        // 3. Credit Wallet via Interface
        DepositRequest deposit = new DepositRequest();
        deposit.setAmount(tx.getAmount());
        deposit.setCurrency("KES");

        walletService.depositFunds(tx.getUser().getId(), deposit);

        log.info("STK Payment Success: User={}, Receipt={}", tx.getUser().getEmail(), receipt);
    }

    private void handleFailure(MpesaTransaction tx, MpesaDto.StkCallbackRequest.StkCallback body) {
        tx.setStatus("FAILED");
        tx.setFailureReason(body.getResultDesc());
        mpesaTransactionRepository.save(tx);
        log.warn("STK Payment Failed: User={}, Reason={}", tx.getUser().getEmail(), body.getResultDesc());
    }
}