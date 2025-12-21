package org.example.escrow.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties appProperties;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                // 1. Disable CSRF (Because we are using stateless JWT/APIs, not browser sessions
                .csrf(AbstractHttpConfigurer::disable)

                // 2.Set Session Management to Stateless (No JSESSIONID cookies)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Define Access Rules
                .authorizeHttpRequests(auth ->auth
                        // Allow public access to Auth endpoints (Register, Login, OTP)
                        // We use the prefix from AppProperties to keep it dynamic (e.g., /api/v1/auth/**)
                        .requestMatchers(appProperties.getApi().getPrefix() + "/auth/**").permitAll()

                        // Lock everything else
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
