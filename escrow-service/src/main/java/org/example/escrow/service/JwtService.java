package org.example.escrow.service;

import org.example.escrow.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {

    String generateToken(User user);

    String generateRefreshToken(User user);

    String extractUsername(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}