package org.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

public class JwtUtil {

    private static final SecretKey SECRET_KEY;
    private static final long EXPIRATION_TIME;

    // Static block to load configuration
    static {
        // Use temporary variables to satisfy compiler's "definite assignment" rules
        SecretKey tempKey = null;
        long tempExpiration = 0;

        try {
            Properties props = new Properties();
            try (InputStream input = JwtUtil.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (input == null) {
                    throw new RuntimeException("application.properties not found");
                }
                props.load(input);
            }

            // 1. Load Secret
            String secretString = props.getProperty("jwt.secret");
            if (secretString == null || secretString.trim().isEmpty()) {
                throw new RuntimeException("jwt.secret is missing in application.properties");
            }
            tempKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8));

            // 2. Load Expiration
            String expirationString = props.getProperty("jwt.expiration");
            if (expirationString == null || expirationString.trim().isEmpty()) {
                throw new RuntimeException("jwt.expiration is missing in application.properties");
            }

            try {
                tempExpiration = Long.parseLong(expirationString.trim());
            } catch (NumberFormatException e) {
                throw new RuntimeException("jwt.expiration must be a valid number (milliseconds)");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load JWT configuration", e);
        }

        // Assign final variables exactly once at the end
        SECRET_KEY = tempKey;
        EXPIRATION_TIME = tempExpiration;
    }

    public static String generateToken(String phoneNumber, Long userId) {
        return Jwts.builder()
                .subject(phoneNumber)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String validateTokenAndGetPhone(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid or Expired Token");
        }
    }
}