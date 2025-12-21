package org.example.escrow.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.transaction.InitiateTransactionRequest;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.EscrowTransaction;
import org.example.escrow.model.MerchantService;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.EscrowStatus;
import org.example.escrow.repository.EscrowTransactionRepository;
import org.example.escrow.repository.MerchantServiceRepository;
import org.example.escrow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EscrowTransactionServiceImpl {

    private final EscrowTransactionRepository transactionRepository;
    private final MerchantServiceRepository merchantServiceRepository;
    private final UserRepository userRepository;
    private final AppProperties appProperties;


    @Transactional
    public EscrowTransaction initiateTransaction(UUID buyerId, InitiateTransactionRequest request){
        // 1. Fetch Buyer
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));

        // 2. Fetch Service
        MerchantService service = merchantServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        // 3. Validation: Is service active?
        if (!service.isActive()) {
            throw new BusinessLogicException("This service is currently unavailable.");
        }

        // 4. Validation: Buyer cannot be the Merchant
        if (service.getMerchant().getUser().getId().equals(buyerId)){
            throw new BusinessLogicException("You cannot buy your own service.");
        }

        // 5. Calculate Financials
        // Fee = Price * Configured % (e.g., 0.005 for 0.5%)
        BigDecimal price = service.getPrice();
        BigDecimal feePercentage = new BigDecimal(appProperties.getEscrow().getPlatformFeePercentage());

        BigDecimal platformFee = price.multiply(feePercentage).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = price.add(platformFee);

        // 6. Build Transaction
        EscrowTransaction transaction = EscrowTransaction.builder()
                .buy(buyer) // The buyer field (User)
                .merchant(service.getMerchant())
                .service(service)
                .totalAmount(totalAmount) // Buyer pays Price + Fee
                .platformFee(platformFee)
                .merchantPayout(price) // Merchant gets the full Price (we only take fee on top)
                .currency(service.getCurrency())
                .status(EscrowStatus.CREATED)
                .build();

        return transactionRepository.save(transaction);
    }
}
