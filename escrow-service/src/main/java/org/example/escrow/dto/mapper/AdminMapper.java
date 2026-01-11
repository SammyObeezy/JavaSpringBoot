package org.example.escrow.dto.mapper;

import org.example.escrow.dto.admin.MerchantProfileResponse;
import org.example.escrow.dto.admin.UserResponse;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminMapper {

    UserResponse toUserResponse(User user);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "ownerName", expression = "java(merchant.getUser().getFirstName() + \" \" + merchant.getUser().getLastName())")
    @Mapping(target = "ownerEmail", source = "user.email")
    MerchantProfileResponse toMerchantResponse(MerchantProfile merchant);
}