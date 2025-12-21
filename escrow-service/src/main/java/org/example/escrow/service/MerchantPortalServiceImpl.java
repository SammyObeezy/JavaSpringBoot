package org.example.escrow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.merchant.CreateServiceRequest;
import org.example.escrow.dto.merchant.MerchantOnboardingRequest;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.DuplicateResourceException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.MerchantServiceRepository;
import org.example.escrow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantPortalServiceImpl {

    private final MerchantProfileRepository merchantProfileRepository;
    private final MerchantServiceRepository merchantServiceRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;

    /**
     * Opt-in a standard user to become a Merchant.
     */
    @Transactional
    public MerchantProfile onboardMerchant(UUID userId, MerchantOnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Check if already a merchant
        if (merchantProfileRepository.findByUserId(userId).isPresent()) {
            throw new BusinessLogicException("User is already a registered merchant.");
        }

        // 2. Check duplicate registration number (only if provided)
        if (request.getBusinessRegNo() != null && !request.getBusinessRegNo().isBlank() &&
                merchantProfileRepository.existsByBusinessRegNo(request.getBusinessRegNo())) {
            throw new DuplicateResourceException("Merchant", "registration number", request.getBusinessRegNo());
        }

        // 3. Create Profile
        MerchantProfile profile = MerchantProfile.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessRegNo(request.getBusinessRegNo())
                // Set default commission from config
                .commissionRate(new BigDecimal(appProperties.getEscrow().getPlatformFeePercentage()))
                .build();

        // 4. Upgrade User Role
        user.setRole(UserRole.ROLE_MERCHANT);
        userRepository.save(user);

        return merchantProfileRepository.save(profile);
    }

    /**
     * Allow a merchant to create a new service listing.
     */
    @Transactional
    public MerchantService createService(UUID userId, CreateServiceRequest request) {
        // 1. Find Merchant Profile linked to this User
        MerchantProfile merchant = merchantProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessLogicException("User is not a registered merchant. Please opt-in first."));

        // 2. Create Service
        MerchantService service = MerchantService.builder()
                .merchant(merchant)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : appProperties.getEscrow().getDefaultCurrency())
                .active(true)
                .build();

        return merchantServiceRepository.save(service);
    }
}