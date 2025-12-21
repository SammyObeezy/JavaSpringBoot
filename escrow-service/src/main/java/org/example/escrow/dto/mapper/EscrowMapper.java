package org.example.escrow.dto.mapper;


import org.example.escrow.dto.transaction.TransactionResponse;
import org.example.escrow.model.EscrowTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EscrowMapper {

    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "merchantName", source = "merchant.businessName")
    @Mapping(target = "itemPrice", source = "service.price")
    @Mapping(target = "totalPay", source = "totalAmount")
    TransactionResponse toResponse(EscrowTransaction transaction);
}
