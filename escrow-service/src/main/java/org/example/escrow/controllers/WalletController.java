package org.example.escrow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.dto.mapper.WalletMapper;
import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.dto.wallet.WalletResponse;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.Wallet;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.service.WalletServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${app.config.api.prefix}/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletServiceImpl walletService;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<WalletResponse>> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositRequest request) {

        UUID userId = getUserIdFromDetails(userDetails);

        // 1. Process Logic
        Wallet updatedWallet = walletService.depositFunds(userId, request);

        // 2. Map to Safe DTO
        WalletResponse response = walletMapper.toResponse(updatedWallet);

        return new ResponseEntity<>(
                ApiResponse.success(response, "Funds deposited successfully."),
                HttpStatus.OK
        );
    }

    private UUID getUserIdFromDetails(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email))
                .getId();
    }
}