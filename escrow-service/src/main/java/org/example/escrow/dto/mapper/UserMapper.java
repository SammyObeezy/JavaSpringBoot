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
    @Mapping(target = "phoneVerified", source = "phoneVerified")
    @Mapping(target = "kycVerified", constant = "false")
    @Mapping(target = "accessToken", ignore = true)
    AuthResponse toAuthResponse(User user);

    // DTO to Entity
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "phoneVerified", constant = "false")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    // These should map automatically, but being explicit helps debugging
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    User toEntity(RegisterRequest request);
}