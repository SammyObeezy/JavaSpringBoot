package org.example.escrow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.identity.AuthResponse;
import org.example.escrow.dto.identity.RegisterRequest;
import org.example.escrow.dto.mapper.UserMapper;
import org.example.escrow.exception.DuplicateResourceException;
import org.example.escrow.model.User;
import org.example.escrow.model.Wallet;
import org.example.escrow.model.enums.WalletType;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.repository.WalletRepository;
import org.example.escrow.util.ValidationUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final OtpService otpService;

    @Override
    @Transactional // ACID: If wallet creation fails, User is rolled back.
    public AuthResponse register(RegisterRequest request){
        // 1. Sanitize Phone Number (07xx -> 2547xx)
        String sanitizedPhone = ValidationUtils.sanitizePhoneNumber(request.getPhoneNumber());
        request.setPhoneNumber(sanitizedPhone);

        // 2. Check for Duplicates (Fail Fast)
        if (userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())){
            throw new DuplicateResourceException("User", "phone", request.getPhoneNumber());
        }

        // 3. Map DTO to Entity
        User user = userMapper.toEntity(request);

        // 4. Security: Hash the password
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // 5. Save User
        User savedUser = userRepository.save(user);

        // 6. Create Default Wallet (Every user needs a KES wallet)
        createDefaultWallet(savedUser);

        // 7. NEW: Generate and Send OTP via SMS
        otpService.generateAndSendOtp(savedUser);

        // 8. Return Response
        return userMapper.toAuthResponse(savedUser);
    }

    private void createDefaultWallet(User user){
        Wallet wallet = Wallet.builder()
                .user(user)
                .currency(appProperties.getEscrow().getDefaultCurrency()) // e.g., KES
                .balance(BigDecimal.ZERO)
                .walletType(WalletType.PERSONAL)
                .build();

        walletRepository.save(wallet);
    }
}
