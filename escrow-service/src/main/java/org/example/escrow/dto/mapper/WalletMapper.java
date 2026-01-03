package org.example.escrow.dto.mapper;

import org.example.escrow.dto.wallet.WalletResponse;
import org.example.escrow.model.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletMapper {

    @Mapping(target = "walletId", source = "id")
    @Mapping(target = "ownerName", expression = "java(wallet.getUser().getFirstName() + \" \" + wallet.getUser().getLastName())")
    WalletResponse toResponse(Wallet wallet);
}