package org.example.escrow.service;

import lombok.RequiredArgsConstructor;
import org.example.escrow.dto.admin.MerchantProfileResponse;
import org.example.escrow.dto.admin.UserResponse;
import org.example.escrow.dto.mapper.AdminMapper;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.VerificationStatus;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final AdminMapper adminMapper;

    // --- READ (With Pagination) ---

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(adminMapper::toUserResponse);
    }

    @Transactional(readOnly = true)
    public Page<MerchantProfileResponse> getAllMerchants(Pageable pageable) {
        return merchantProfileRepository.findAll(pageable)
                .map(adminMapper::toMerchantResponse);
    }

    // --- UPDATE (Admin Actions) ---

    /**
     * Ban or Activate a user.
     * In financial systems, we rarely DELETE. We deactivate.
     */
    @Transactional
    public UserResponse updateUserStatus(UUID userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(active);
        User savedUser = userRepository.save(user);

        return adminMapper.toUserResponse(savedUser);
    }

    /**
     * Verify or Reject a Merchant.
     * Updated to support looking up by either MerchantProfile ID OR User ID.
     */
    @Transactional
    public MerchantProfileResponse verifyMerchant(UUID id, VerificationStatus status) {
        // Try to find by MerchantProfile ID first, if not found, try by User ID
        MerchantProfile profile = merchantProfileRepository.findById(id)
                .or(() -> merchantProfileRepository.findByUserId(id))
                .orElseThrow(() -> new ResourceNotFoundException("Merchant Profile", "id or userId", id));

        profile.setVerificationStatus(status);
        MerchantProfile savedProfile = merchantProfileRepository.save(profile);

        return adminMapper.toMerchantResponse(savedProfile);
    }
}