package org.example.escrow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.admin.MerchantProfileResponse;
import org.example.escrow.dto.identity.ApiResponse;
import org.example.escrow.dto.mapper.MerchantMapper;
import org.example.escrow.dto.merchant.CreateServiceRequest;
import org.example.escrow.dto.merchant.MerchantOnboardingRequest;
import org.example.escrow.dto.merchant.MerchantServiceResponse;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;
import org.example.escrow.model.User;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.service.MerchantPortalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("${app.config.api.prefix}/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantPortalService merchantPortalService;
    private final UserRepository userRepository;
    private final MerchantMapper merchantMapper;

    @PostMapping("/onboard")
    public ResponseEntity<ApiResponse<MerchantProfileResponse>> onboardMerchant(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MerchantOnboardingRequest request) {

        UUID userId = getUserIdFromDetails(userDetails);

        MerchantProfile profile = merchantPortalService.onboardMerchant(userId, request);

        MerchantProfileResponse response = merchantMapper.toResponse(profile);

        return new ResponseEntity<>(
                ApiResponse.success(response, "Merchant profile created successfully."),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/services")
    public ResponseEntity<ApiResponse<MerchantServiceResponse>> createService(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateServiceRequest request) {

        UUID userId = getUserIdFromDetails(userDetails);

        MerchantService service = merchantPortalService.createService(userId, request);

        MerchantServiceResponse response = merchantMapper.toResponse(service);

        return new ResponseEntity<>(
                ApiResponse.success(response, "Service added successfully."),
                HttpStatus.CREATED
        );
    }

    private UUID getUserIdFromDetails(UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return user.getId();
    }
}