package org.example.escrow.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.dto.mapper.EscrowMapper;
import org.example.escrow.dto.transaction.InitiateTransactionRequest;
import org.example.escrow.dto.transaction.TransactionResponse;
import org.example.escrow.model.EscrowTransaction;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.service.EscrowTransactionServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${app.config.api.prefix}/transactions")
@RequiredArgsConstructor
public class EscrowController {

    private final EscrowTransactionServiceImpl transactionService;
    private final UserRepository userRepository;
    private final EscrowMapper escrowMapper;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<TransactionResponse>> initiateTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InitiateTransactionRequest request) {

        UUID buyerId = getUserIdFromDetails(userDetails);

        EscrowTransaction transaction = transactionService.initiateTransaction(buyerId, request);
        TransactionResponse response = escrowMapper.toResponse(transaction);

        return new ResponseEntity<>(
                ApiResponse.success(response, "Transaction initiated. Waiting for payment."),
                HttpStatus.CREATED
        );
    }

    private UUID getUserIdFromDetails(UserDetails userDetails){
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"))
                .getId();
    }
}
