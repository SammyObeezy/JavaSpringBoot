package org.example.escrow.service;

import org.example.escrow.dto.merchant.CreateServiceRequest;
import org.example.escrow.dto.merchant.MerchantOnboardingRequest;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;

import java.util.UUID;

public interface MerchantPortalService {
    MerchantProfile onboardMerchant(UUID userId, MerchantOnboardingRequest request);

    MerchantService createService(UUID userId, CreateServiceRequest request);
}