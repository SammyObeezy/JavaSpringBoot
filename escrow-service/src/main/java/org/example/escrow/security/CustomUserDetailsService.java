package org.example.escrow.security;

import lombok.RequiredArgsConstructor;
import org.example.escrow.model.User;
import org.example.escrow.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // 1. Try to find by Email
        Optional<User> userOptional = userRepository.findByEmail(identifier);

        // 2. If not found, try to find by Phone Number
        if (userOptional.isEmpty()) {
            userOptional = userRepository.findByPhoneNumber(identifier);
        }

        return userOptional
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail()) // Always set Email as the principal username for consistency
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name().replace("ROLE_", ""))
                        .disabled(!user.isActive())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or phone: " + identifier));
    }
}