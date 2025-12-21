package org.example.escrow.dto.mapper;

import org.example.escrow.dto.merchant.MerchantServiceResponse;
import org.example.escrow.model.MerchantService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MerchantMapper {

    @Mapping(target = "serviceId", source = "id")
    MerchantServiceResponse toResponse(MerchantService entity);
}