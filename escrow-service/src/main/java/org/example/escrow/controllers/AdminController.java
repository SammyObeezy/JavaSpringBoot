package org.example.escrow.controllers;

import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.admin.MerchantProfileResponse;
import org.example.escrow.dto.admin.UserResponse;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.service.AdminService;
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
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return new ResponseEntity<>(
                ApiResponse.success(users, "Retrieved all users."),
                HttpStatus.OK
        );
    }

    @GetMapping("/merchants")
    public ResponseEntity<ApiResponse<List<MerchantProfileResponse>>> getAllMerchants() {
        List<MerchantProfileResponse> merchants = adminService.getAllMerchants();
        return new ResponseEntity<>(
                ApiResponse.success(merchants, "Retrieved all merchants."),
                HttpStatus.OK
        );
    }
}