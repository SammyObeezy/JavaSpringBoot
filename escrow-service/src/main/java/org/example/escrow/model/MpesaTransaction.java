package org.example.escrow.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "mpesa_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MpesaTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Unique ID from Safaricom used to match the callback
    @Column(name = "checkout_request_id", unique = true, nullable = false)
    private String checkoutRequestId;

    @Column(name = "merchant_request_id")
    private String merchantRequestId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "mpesa_receipt_number")
    private String mpesaReceiptNumber;

    // Status: PENDING, COMPLETED, FAILED
    @Column(nullable = false)
    private String status;

    @Column(name = "failure_reason")
    private String failureReason;
}