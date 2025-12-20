package org.example.escrow.dto.mapper;


import org.example.escrow.dto.identity.AuthResponse;
import org.example.escrow.dto.identity.RegisterRequest;
import org.example.escrow.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    // Entity to DTO
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "isPhoneVerified", source = "phoneVerified")
    // Note: isKycVerified logic often depends on MerchantProfile,
    // usually mapped in a Service layer or using @AfterMapping,
    // but for now we map basic User fields.
    @Mapping(target = "isKycVerified", constant = "false") // Default for new users
    @Mapping(target = "accessToken", ignore = true) // Token is generated separately
    AuthResponse toAuthResponse(User user);

    // DTO to Entity
    @Mapping(target = "passwordHash", ignore = true) // Handles manually (encoding)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "phoneVerified", constant = "false")
    // Sanitization: We assume input is sanitized, or use a qualifiedByName method
    User toEntity(RegisterRequest request);
}
