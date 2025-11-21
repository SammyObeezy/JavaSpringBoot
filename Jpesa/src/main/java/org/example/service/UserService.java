package org.example.service;

import org.example.dto.RegisterRequest;
import org.example.model.User;
import org.example.model.Wallet;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.example.util.InputValidator;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    // Constructor Injection (Easy to test)
    public UserService() {
        this.userRepository = new UserRepository();
        this.walletRepository = new WalletRepository();
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
}
