package org.example.escrow.dto.mapper;

import org.example.escrow.dto.admin.MerchantProfileResponse;
import org.example.escrow.dto.merchant.MerchantServiceResponse;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MerchantMapper {

    @Mapping(target = "serviceId", source = "id")
    MerchantServiceResponse toResponse(MerchantService entity);

    // NEW: Map Profile to DTO to avoid recursion and lazy loading errors
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "ownerName", expression = "java(profile.getUser().getFirstName() + \" \" + profile.getUser().getLastName())")
    @Mapping(target = "ownerEmail", source = "user.email")
    MerchantProfileResponse toResponse(MerchantProfile profile);
}