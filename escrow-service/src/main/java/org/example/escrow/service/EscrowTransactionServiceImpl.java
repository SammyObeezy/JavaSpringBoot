package org.example.escrow.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.mapper.EscrowMapper;
import org.example.escrow.dto.transaction.InitiateTransactionRequest;
import org.example.escrow.dto.transaction.TransactionResponse;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.EscrowTransaction;
import org.example.escrow.model.MerchantProfile;
import org.example.escrow.model.MerchantService;
import org.example.escrow.model.User;
import org.example.escrow.model.enums.EscrowStatus;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.repository.EscrowTransactionRepository;
import org.example.escrow.repository.MerchantProfileRepository;
import org.example.escrow.repository.MerchantServiceRepository;
import org.example.escrow.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EscrowTransactionServiceImpl {

    private final EscrowTransactionRepository transactionRepository;
    private final MerchantServiceRepository merchantServiceRepository;
    private final UserRepository userRepository;
    private final MerchantProfileRepository merchantProfileRepository; // Added for Merchant lookup
    private final AppProperties appProperties;
    private final WalletServiceImpl walletService;
    private final EscrowMapper escrowMapper;

    @Transactional
    public TransactionResponse initiateTransaction(UUID buyerId, InitiateTransactionRequest request) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));

        MerchantService service = merchantServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", request.getServiceId()));

        if (!service.isActive()) {
            throw new BusinessLogicException("This service is currently unavailable.");
        }

        if (service.getMerchant().getUser().getId().equals(buyerId)) {
            throw new BusinessLogicException("You cannot buy your own service.");
        }

        BigDecimal price = service.getPrice();
        BigDecimal feePercentage = new BigDecimal(appProperties.getEscrow().getPlatformFeePercentage());

        BigDecimal platformFee = price.multiply(feePercentage).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = price.add(platformFee);

        EscrowTransaction transaction = EscrowTransaction.builder()
                .buy(buyer)
                .merchant(service.getMerchant())
                .service(service)
                .totalAmount(totalAmount)
                .platformFee(platformFee)
                .merchantPayout(price)
                .currency(service.getCurrency())
                .status(EscrowStatus.CREATED)
                .build();

        EscrowTransaction saved = transactionRepository.save(transaction);
        return escrowMapper.toResponse(saved);
    }

    @Transactional
    public TransactionResponse payTransaction(UUID buyerId, UUID transactionId) {
        EscrowTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));

        if (!transaction.getBuy().getId().equals(buyerId)) {
            throw new BusinessLogicException("Unauthorized. This transaction does not belong to you.");
        }
        if (transaction.getStatus() != EscrowStatus.CREATED) {
            throw new BusinessLogicException("Transaction is not in a payable state (Current: " + transaction.getStatus() + ")");
        }

        walletService.deductFunds(
                buyerId,
                transaction.getTotalAmount(),
                transaction.getCurrency(),
                "Escrow Payment for TX: " + transaction.getId()
        );

        transaction.setStatus(EscrowStatus.PAID);
        EscrowTransaction saved = transactionRepository.save(transaction);

        return escrowMapper.toResponse(saved);
    }

    // --- NEW METHOD FOR HISTORY ---
    public List<TransactionResponse> getTransactionHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        List<EscrowTransaction> transactions;

        if (user.getRole() == UserRole.ROLE_ADMIN) {
            // Admin sees EVERYTHING
            transactions = transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        else if (user.getRole() == UserRole.ROLE_MERCHANT) {
            // Merchant sees sales where they are the seller
            MerchantProfile profile = merchantProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BusinessLogicException("Merchant profile not found"));
            transactions = transactionRepository.findByMerchantIdOrderByCreatedAtDesc(profile.getId());
        }
        else {
            // Standard User sees purchases they made
            transactions = transactionRepository.findByBuyIdOrderByCreatedAtDesc(user.getId());
        }

        // Convert to DTOs
        // Note: Because of Lazy Loading, we rely on the Service method being @Transactional
        // OR the entities being initialized. Since this is a read-only Fetch,
        // passing through Mapper inside the Transactional scope (or Open-Session-In-View) usually works.
        // To be safe, we map here.
        return transactions.stream()
                .map(transaction -> {
                    // Safe access to ensure lazy fields are loaded
                    if (transaction.getService() != null) transaction.getService().getName();
                    if (transaction.getMerchant() != null) transaction.getMerchant().getBusinessName();
                    return escrowMapper.toResponse(transaction);
                })
                .collect(Collectors.toList());
    }
}