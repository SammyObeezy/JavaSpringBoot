package org.example.escrow.security;

import lombok.RequiredArgsConstructor;
import org.example.escrow.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Loads user from DB and converts to Spring Security User object
        return userRepository.findByEmail(email)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPasswordHash())
                        .roles(user.getRole().name().replace("ROLE_", "")) // e.g. "USER"
                        .disabled(!user.isActive())
                        .build()
                )
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}