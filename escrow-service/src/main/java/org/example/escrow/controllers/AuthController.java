package org.example.escrow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.identity.*;
import org.example.escrow.service.AuthService;
import org.example.escrow.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // Used for initial registration verification
    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getCode());
        return new ResponseEntity<>(
                ApiResponse.success(null, "Phone number verified successfully."),
                HttpStatus.OK
        );
    }

    // Step 1: Login (Password Check -> Sends OTP)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody LoginRequest request) {
        authService.initiateLogin(request);
        return new ResponseEntity<>(
                ApiResponse.success(null, "Credentials valid. OTP sent to your phone."),
                HttpStatus.ACCEPTED
        );
    }

    // Step 2: Login Verify (OTP Check -> Returns Token)
    @PostMapping("/login/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLogin(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyLogin(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Login successful."),
                HttpStatus.OK
        );
    }
}