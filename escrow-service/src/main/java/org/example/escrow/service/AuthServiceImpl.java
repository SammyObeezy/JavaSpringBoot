package org.example.escrow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.example.escrow.service.JwtService;
import org.example.escrow.util.ValidationUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
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

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
        );

        String finalIdentifier = identifier;
        User user = userRepository.findByEmail(finalIdentifier)
                .or(() -> userRepository.findByPhoneNumber(finalIdentifier))
                .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", finalIdentifier));

        if (!user.isPhoneVerified()) {
            throw new BusinessLogicException("Account not verified. Please verify your registration OTP first.");
        }

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

    @Override
    public void logout(String token) {
        String jwt = token;
        if (token != null && token.startsWith("Bearer ")) {
            jwt = token.substring(7);
        }

        String username = jwtService.extractUsername(jwt);
        log.info("User logged out: {}", username);

        // 2. Clear Context (Best practice, though redundant in stateless per-request filter)
        SecurityContextHolder.clearContext();

        // 3. TODO: Add Token Blacklisting here (Redis)
        // tokenBlacklistService.blacklist(jwt);
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