package org.example.escrow.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.dto.identity.AuthResponse;
import org.example.escrow.dto.identity.RegisterRequest;
import org.example.escrow.dto.identity.VerifyOtpRequest;
import org.example.escrow.service.AuthService;
import org.example.escrow.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Public Authentication endpoints.
 * Base Path: /api/v1/auth
 */
@RestController
@RequestMapping("${app.config.api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request){
        AuthResponse response = authService.register(request);

        return new ResponseEntity<>(
                ApiResponse.success(response, "User registered successfully. Please verify your phone."),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(@Valid @RequestBody VerifyOtpRequest request){
        otpService.verifyOtp(request.getEmail(), request.getCode());
        return new ResponseEntity<>(
                ApiResponse.success(null, "Phone number verified successfully."),
                HttpStatus.OK
        );
    }
}
