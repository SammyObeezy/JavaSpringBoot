package org.example.escrow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.dto.transaction.InitiateTransactionRequest;
import org.example.escrow.dto.transaction.TransactionResponse;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.service.EscrowTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${app.config.api.prefix}/transactions")
@RequiredArgsConstructor
public class EscrowController {

    // Updated: Inject Interface instead of Implementation
    private final EscrowTransactionService transactionService;

    private final UserRepository userRepository;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<TransactionResponse>> initiateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InitiateTransactionRequest request) {

        UUID buyerId = getUserIdFromDetails(userDetails);

        TransactionResponse response = transactionService.initiateTransaction(buyerId, request);

        return new ResponseEntity<>(
                ApiResponse.success(response, "Transaction initiated. Waiting for payment."),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/{transactionId}/pay")
    public ResponseEntity<ApiResponse<TransactionResponse>> payTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID transactionId) {

        UUID buyerId = getUserIdFromDetails(userDetails);

        TransactionResponse response = transactionService.payTransaction(buyerId, transactionId);

        return new ResponseEntity<>(
                ApiResponse.success(response, "Payment successful. Funds held in Escrow."),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<TransactionResponse> history = transactionService.getTransactionHistory(userDetails.getUsername());

        return new ResponseEntity<>(
                ApiResponse.success(history, "Transaction history retrieved."),
                HttpStatus.OK
        );
    }

    private UUID getUserIdFromDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();
    }
}