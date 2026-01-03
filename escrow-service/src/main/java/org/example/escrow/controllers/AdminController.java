package org.example.escrow.controllers;

import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.User;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${app.config.api.prefix}/admin")
@RequiredArgsConstructor
// Enforce strict security: Only ADMINs can access this controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final MerchantProfileRepository merchantProfileRepository;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new ResponseEntity<>(
                ApiResponse.success(users, "Retrieved all users."),
                HttpStatus.OK
        );
    }

    @GetMapping("/merchants")
    public ResponseEntity<ApiResponse<List<MerchantProfile>>> getAllMerchants() {
        List<MerchantProfile> merchants = merchantProfileRepository.findAll();
        return new ResponseEntity<>(
                ApiResponse.success(merchants, "Retrieved all merchants."),
                HttpStatus.OK
        );
    }
}