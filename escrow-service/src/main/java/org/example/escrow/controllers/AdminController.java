package org.example.escrow.controllers;

import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.admin.MerchantProfileResponse;
import org.example.escrow.dto.admin.UserResponse;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.model.enums.VerificationStatus;
import org.example.escrow.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${app.config.api.prefix}/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;


    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<UserResponse> users = adminService.getAllUsers(pageable);
        return new ResponseEntity<>(
                ApiResponse.success(users, "Retrieved users page."),
                HttpStatus.OK
        );
    }

    @GetMapping("/merchants")
    public ResponseEntity<ApiResponse<Page<MerchantProfileResponse>>> getAllMerchants(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<MerchantProfileResponse> merchants = adminService.getAllMerchants(pageable);
        return new ResponseEntity<>(
                ApiResponse.success(merchants, "Retrieved merchants page."),
                HttpStatus.OK
        );
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable UUID userId,
            @RequestParam boolean active) {

        UserResponse response = adminService.updateUserStatus(userId, active);
        String message = active ? "User activated." : "User deactivated.";

        return new ResponseEntity<>(
                ApiResponse.success(response, message),
                HttpStatus.OK
        );
    }

    @PutMapping("/merchants/{merchantId}/verify")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> verifyMerchant(
            @PathVariable UUID merchantId,
            @RequestParam VerificationStatus status) {

        MerchantProfileResponse response = adminService.verifyMerchant(merchantId, status);

        return new ResponseEntity<>(
                ApiResponse.success(response, "Merchant status updated to " + status),
                HttpStatus.OK
        );
    }
}