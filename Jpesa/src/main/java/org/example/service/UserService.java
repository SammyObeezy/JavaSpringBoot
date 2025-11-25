package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.config.RedisConfig;
import org.example.dto.LoginRequest;
import org.example.dto.PasswordResetRequest;
import org.example.dto.RegisterRequest;
import org.example.dto.VerifyOtpRequest;
import org.example.model.*;
import org.example.repository.OtpRepository;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.example.util.InputValidator;
import org.mindrot.jbcrypt.BCrypt;
import redis.clients.jedis.Jedis;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final OtpRepository otpRepository;
    private final ObjectMapper objectMapper;

    // 1. Default Constructor (Used by the App)
    public UserService() {
        this(new UserRepository(), new WalletRepository(), new OtpRepository());
    }

    // 2. Parameterized Constructor (Used by Tests for Injection)
    public UserService(UserRepository userRepository, WalletRepository walletRepository, OtpRepository otpRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.otpRepository = otpRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Register a new user and automatically creates a wallet for them.
     * @param request The raw registration data
     * @return The created User object (without password)
     */
    public User registerUser(RegisterRequest request){
        // 1. Validate & Normalize Inputs
        validateRequest(request);
        String normalizedPhone = InputValidator.formatPhoneNumber(request.getPhoneNumber());

        // 2. Check if User Exists
        Optional<User> existingUser = userRepository.findByPhoneNumber(normalizedPhone);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with phone number " + normalizedPhone + " already exists.");
        }

        // 3. Hash Password (Security)
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12));

        // 4. Create User Object
        User newUser = new User(
                request.getFullName(),
                normalizedPhone,
                request.getEmail(),
                hashedPassword
        );

        // 5. Save User to DB
        User savedUser = userRepository.save(newUser);

        // 6. Create & Save Wallet (Every user needs a wallet)
        Wallet newWallet = new Wallet(savedUser.getUserId());
        walletRepository.save(newWallet);

        // 7. Return result (Hide hash in response generally, but for now we return the object)
        return savedUser;
    }

    /**
     * Authenticate a user and generate an OTP
     * @request String message indicating OTP sent (In real life, returns a session or triggers SMS)
     */
    public String loginUser(LoginRequest request){
        // 1. Validate Phone Format
        String normalizedPhone = InputValidator.formatPhoneNumber(request.getPhoneNumber());

        // 2. Find User
        User user = userRepository.findByPhoneNumber(normalizedPhone)
                .orElseThrow(()-> new IllegalArgumentException("Invalid phone number or password")); // Generic error for security

        //3. Check Password (BCrypt)
        if (!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())){
            // In a real app, we would log this failed attempt in audit_logs
            throw new IllegalArgumentException("Invalid phone number or password");
        }

        //4. Check Status
        if (user.getStatus() != UserStatus.ACTIVE){
            throw new IllegalStateException("Account is " + user.getStatus());
        }

        //5. Generate 6-Digit OTP
        String otpCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        // 6. Save OTP to DB (Expires in 5 mins)
        OtpCode otp = new OtpCode(user.getUserId(), otpCode, OtpPurpose.LOGIN_VERIFICATION, 5);
        otpRepository.save(otp);

        // 7. Simulate Sending SMS
        System.out.println(">>> [SMS GATEWAY] Sending OTP " + otpCode + " to " + normalizedPhone);

        return "OTP sent to " + normalizedPhone + ". Please verify.";
    }



    /*
    * Validates individual fields using our InputValidator utility.
     */
    private void validateRequest(RegisterRequest request){
        if (request.getFullName() == null || request.getPhoneNumber().trim().isEmpty()){
            throw new IllegalArgumentException("Full name is required");
        }

        // This will throw IllegalArgumentException of phone is invalid
        InputValidator.formatPhoneNumber(request.getPhoneNumber());

        if (request.getEmail() != null && !request.getEmail().isEmpty()){
            if (!InputValidator.isValidEmail(request.getEmail())){
                throw new IllegalArgumentException("Invalid email address");
            }
        }

        if (!InputValidator.isValidPassword(request.getPassword())){
            throw new IllegalArgumentException("Password must be at least 8 characters, contain a letter and a number.");
        }
    }
    /**
     * Verifies the OTP provided by the user.
     * @return the user object if verification is successful
     */
    public User verifyOtp(VerifyOtpRequest request){
        // 1. Validate Input
        String normalizedPhone = InputValidator.formatPhoneNumber(request.getPhoneNumber());

        // 2. Find User first (to get the ID)
        User user = userRepository.findByPhoneNumber(normalizedPhone)
                .orElseThrow(()-> new IllegalArgumentException("User not found"));

        // 3. Check OTP Database
        OtpCode validOtp = otpRepository.findValidOtp(user.getUserId(), request.getOtpCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or Expired OTP"));

        // 4. Mark OTP as used (Security: Prevent replay attacks)
        otpRepository.markAsUsed(validOtp.getOtpId());

        System.out.println(">>> [AUTH] User " + user.getPhoneNumber() + " logged in successfully.");

        return user;
    }

    /**
     * Step 1: User asks for a reset. We verify they exist and send an OTP.
     */
    public String initiatePasswordReset(String phoneNumber){
        String normalizedPhone = InputValidator.formatPhoneNumber(phoneNumber);
        User user = userRepository.findByPhoneNumber(normalizedPhone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate OTP
        String otpCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        // Save OTP with purpose PASSWORD_REST
        OtpCode otp = new OtpCode(user.getUserId(), otpCode, OtpPurpose.PASSWORD_RESET, 10);
        otpRepository.save(otp);

        System.out.println(">>> [SMS GATEWAY] RESET OTP " + otpCode + " sent to " + normalizedPhone);
        return "OTP sent for password reset.";
    }

    /**
     * Step 2: User provides OTP and New Password. We verify and update
     */
    public void completePasswordReset(PasswordResetRequest request){
        // 1. Validate Input
        String normalizedPhone = InputValidator.formatPhoneNumber(request.getPhoneNumber());
        if (!InputValidator.isValidPassword(request.getNewPassword())){
            throw new IllegalArgumentException("Password too weak");
        }

        // 2. Find User
        User user = userRepository.findByPhoneNumber(normalizedPhone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3. Find Valid OTP (Must be for PASSWORD_RESET purpose
        OtpCode validOtp = otpRepository.findValidOtp(user.getUserId(), request.getOtpCode())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or Expired OTP"));

        // 4. Check Purpose (Security check)
        if (validOtp.getPurpose() != OtpPurpose.PASSWORD_RESET){
            throw new IllegalArgumentException("Invalid OTP purpose");
        }

        // 5. Update Password
        String newHash = BCrypt.hashpw(request.getNewPassword(), BCrypt.gensalt(12));
        userRepository.updatePassword(user.getUserId(), newHash);

        // 6. Mark OTP used
        otpRepository.markAsUsed(validOtp.getOtpId());

        System.out.println(">>> [AUTH] Password reset successful for " + normalizedPhone);
    }
    /**
     * CACHED USER LOOKUP
     * Checks Redis first. If missing, hits DB and saves to Redis.
     */
    public User getCachedUser(String phoneNumber) {
        String normalizedPhone = InputValidator.formatPhoneNumber(phoneNumber);
        String cacheKey = "user:" + normalizedPhone;

        // 1. CHECK REDIS (Fast Lane)
        try (Jedis redis = RedisConfig.getConnection()) {
            String cachedJson = redis.get(cacheKey);
            if (cachedJson != null) {
                System.out.println(">>> [CACHE HIT] Found in Redis: " + normalizedPhone);
                return objectMapper.readValue(cachedJson, User.class);
            }
        } catch (Exception e) {
            System.err.println("Redis error (ignoring): " + e.getMessage());
        }

        // 2. QUERY DB (Slow Lane)
        System.out.println(">>> [CACHE MISS] Querying Database: " + normalizedPhone);
        User user = userRepository.findByPhoneNumber(normalizedPhone)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3. SAVE TO REDIS (For next time)
        try (Jedis redis = RedisConfig.getConnection()) {
            String json = objectMapper.writeValueAsString(user);
            redis.setex(cacheKey, RedisConfig.TTL_SECONDS, json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return user;
    }
    public void invalidateCache(String phoneNumber){
        String normalizedPhone = InputValidator.formatPhoneNumber(phoneNumber);
        try (Jedis redis = RedisConfig.getConnection()){
            redis.del("user:" + normalizedPhone);
            System.out.println(">>> [CACHE CLEARED] for " + normalizedPhone);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
