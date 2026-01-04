package org.example.escrow.service;

import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.merchant.CreateServiceRequest;
import org.example.escrow.dto.merchant.MerchantOnboardingRequest;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.MerchantServiceRepository;
import org.example.escrow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantPortalServiceImplTest {

    @Mock private MerchantProfileRepository merchantProfileRepository;
    @Mock private MerchantServiceRepository merchantServiceRepository;
    @Mock private UserRepository userRepository;
    @Mock private AppProperties appProperties;
    @Mock private AppProperties.Escrow escrowProps;

    @InjectMocks
    private MerchantPortalServiceImpl merchantService;

    private User user;
    private MerchantProfile profile;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        // Fixed: Set ID manually because @Builder doesn't see BaseEntity fields
        user = User.builder()
                .email("merchant@test.com")
                .role(UserRole.ROLE_USER)
                .firstName("Merchant") // Added required fields
                .lastName("Test")
                .build();
        user.setId(userId);

        // Fixed: Set ID manually
        profile = MerchantProfile.builder()
                .user(user)
                .build();
        profile.setId(UUID.randomUUID());

        lenient().when(appProperties.getEscrow()).thenReturn(escrowProps);
        lenient().when(escrowProps.getPlatformFeePercentage()).thenReturn(0.05);
        lenient().when(escrowProps.getDefaultCurrency()).thenReturn("KES");
    }

    @Test
    void onboardMerchant_ShouldSuccess_WhenNew() {
        MerchantOnboardingRequest request = new MerchantOnboardingRequest("My Shop", "REG123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.empty()); // Not yet a merchant
        when(merchantProfileRepository.save(any(MerchantProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        MerchantProfile result = merchantService.onboardMerchant(userId, request);

        assertNotNull(result);
        assertEquals("My Shop", result.getBusinessName());
        verify(userRepository).save(user); // Should update role
        assertEquals(UserRole.ROLE_MERCHANT, user.getRole());
    }

    @Test
    void onboardMerchant_ShouldThrow_WhenAlreadyMerchant() {
        MerchantOnboardingRequest request = new MerchantOnboardingRequest("My Shop", "REG123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile)); // Already exists

        assertThrows(BusinessLogicException.class, () -> merchantService.onboardMerchant(userId, request));
    }

    @Test
    void createService_ShouldSuccess_WhenMerchantExists() {
        CreateServiceRequest request = new CreateServiceRequest();
        request.setName("Web Design");
        request.setPrice(BigDecimal.valueOf(5000));

        when(merchantProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(merchantServiceRepository.save(any(MerchantService.class))).thenAnswer(i -> i.getArguments()[0]);

        MerchantService result = merchantService.createService(userId, request);

        assertNotNull(result);
        assertEquals("Web Design", result.getName());
        assertEquals("KES", result.getCurrency()); // Default currency check
    }
}