package org.example.escrow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.identity.*;
import org.example.escrow.model.enums.NotificationChannel;
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
                ApiResponse.success(response, "User registered successfully. Please verify your account."),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(@Valid @RequestBody VerifyOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getCode());
        return new ResponseEntity<>(
                ApiResponse.success(null, "Account verified successfully."), // Updated message
                HttpStatus.OK
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        NotificationChannel channel = request.getChannel() != null ? request.getChannel() : NotificationChannel.SMS;
        otpService.resendOtp(request.getEmail(), channel);

        return new ResponseEntity<>(
                ApiResponse.success(null, "OTP resent successfully via " + channel),
                HttpStatus.OK
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@RequestBody LoginRequest request) {
        // Removed @Valid because fields are optional in DTO now, validated in service
        authService.initiateLogin(request);
        return new ResponseEntity<>(
                ApiResponse.success(null, "Credentials valid. OTP sent."),
                HttpStatus.ACCEPTED
        );
    }

    @PostMapping("/login/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyLogin(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyLogin(request);
        return new ResponseEntity<>(
                ApiResponse.success(response, "Login successful."),
                HttpStatus.OK
        );
    }
}