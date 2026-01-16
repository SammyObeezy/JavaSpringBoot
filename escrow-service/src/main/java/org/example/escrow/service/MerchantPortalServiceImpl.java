package org.example.escrow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.merchant.CreateServiceRequest;
import org.example.escrow.dto.merchant.MerchantOnboardingRequest;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.MerchantServiceRepository;
import org.example.escrow.repository.UserRepository;
import org.example.escrow.util.EncryptionUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantPortalServiceImpl implements MerchantPortalService {

    private final MerchantProfileRepository merchantProfileRepository;
    private final MerchantServiceRepository merchantServiceRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;
    private final EncryptionUtil encryptionUtil;

    @Override
    @Transactional
    public MerchantProfile onboardMerchant(UUID userId, MerchantOnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (merchantProfileRepository.findByUserId(userId).isPresent()) {
            throw new BusinessLogicException("User is already a registered merchant.");
        }

        String encryptedRegNo = encryptionUtil.encrypt(request.getBusinessRegNo());

        MerchantProfile profile = MerchantProfile.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessRegNo(encryptedRegNo)
                .commissionRate(new BigDecimal(appProperties.getEscrow().getPlatformFeePercentage()))
                .build();

        user.setRole(UserRole.ROLE_MERCHANT);
        userRepository.save(user);

        return merchantProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public MerchantService createService(UUID userId, CreateServiceRequest request) {
        MerchantProfile merchant = merchantProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessLogicException("User is not a registered merchant. Please opt-in first."));

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