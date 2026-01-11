package org.example.escrow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.identity.AuthResponse;
import org.example.escrow.dto.identity.LoginRequest;
import org.example.escrow.dto.identity.RegisterRequest;
import org.example.escrow.dto.identity.VerifyOtpRequest;
import org.example.escrow.dto.mapper.UserMapper;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.DuplicateResourceException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.User;
import org.example.escrow.model.Wallet;
import org.example.escrow.model.enums.NotificationChannel;
import org.example.escrow.model.enums.WalletType;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.repository.WalletRepository;
import org.example.escrow.util.ValidationUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request){
        String sanitizedPhone = ValidationUtils.sanitizePhoneNumber(request.getPhoneNumber());
        request.setPhoneNumber(sanitizedPhone);

        if (userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new DuplicateResourceException("User", "phone", request.getPhoneNumber());
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        createDefaultWallet(savedUser);
        otpService.generateAndSendOtp(savedUser);

        return userMapper.toAuthResponse(savedUser);
    }

    @Override
    public void initiateLogin(LoginRequest request) {
        // Determine Identifier and Channel based on input
        String identifier;
        NotificationChannel channel;

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            identifier = request.getEmail();
            channel = NotificationChannel.EMAIL;
        } else if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            identifier = ValidationUtils.sanitizePhoneNumber(request.getPhoneNumber());
            channel = NotificationChannel.SMS;
        } else {
            throw new BusinessLogicException("Email or Phone Number is required for login.");
        }

        // 1. Authenticate (Checks DB using CustomUserDetailsService)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
        );

        // 2. Fetch User to send OTP
        // We reuse the logic from CustomUserDetailsService to find by either
        String finalIdentifier = identifier;
        User user = userRepository.findByEmail(finalIdentifier)
                .or(() -> userRepository.findByPhoneNumber(finalIdentifier))
                .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", finalIdentifier));

        // 3. Security Check
        if (!user.isPhoneVerified()) {
            throw new BusinessLogicException("Account not verified. Please verify your registration OTP first.");
        }

        // 4. Send 2FA OTP (Email or SMS based on login method)
        otpService.generateAndSendOtp(user, channel);
    }

    @Override
    public AuthResponse verifyLogin(VerifyOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getCode());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse response = userMapper.toAuthResponse(user);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        return response;
    }

    private void createDefaultWallet(User user){
        Wallet wallet = Wallet.builder()
                .user(user)
                .currency(appProperties.getEscrow().getDefaultCurrency())
                .balance(BigDecimal.ZERO)
                .walletType(WalletType.PERSONAL)
                .build();
        walletRepository.save(wallet);
    }
}