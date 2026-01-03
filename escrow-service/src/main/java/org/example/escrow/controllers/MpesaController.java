package org.example.escrow.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.dto.mpesa.MpesaDto;
import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.User;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.service.MpesaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${app.config.api.prefix}/mpesa")
@RequiredArgsConstructor
@Slf4j
public class MpesaController {

    private final MpesaService mpesaService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 1. Trigger STK Push
     */
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<MpesaDto.StkPushSyncResponse>> triggerDeposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody DepositRequest request){

        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Pass the actual User object so the service can save the Pending Transaction linked to them
        MpesaDto.StkPushSyncResponse response = mpesaService.initiateStkPush(
                user,
                request.getAmount(),
                user.getId().toString()
        );

        return new ResponseEntity<>(
                ApiResponse.success(response, "STK Push sent. Please enter your PIN."),
                HttpStatus.OK
        );
    }

    /**
     * 2. Receive Callback from Safaricom
     */
    @PostMapping("/callback")
    public ResponseEntity<Void> handleCallback(@RequestBody MpesaDto.StkCallbackRequest callback) throws JsonProcessingException{
        log.info("Received M-Pesa Callback: {}", objectMapper.writeValueAsString(callback));

        // Delegate all logic (Lookup, Validation, Wallet Update) to the Service
        mpesaService.processCallback(callback.getBody().getStkCallback());

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
