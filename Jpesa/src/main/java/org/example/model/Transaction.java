package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction implements TimeTracked {

    private Long transactionId;
    private Long walletId;
    private TransactionType txnType;
    private BigDecimal amount;
    private  String referenceCode;

    // Fields required by TimeTracked interface
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public Transaction() {

    }
    public Transaction(Long walletId, TransactionType txnType, BigDecimal amount, String referenceCode){
        this.walletId = walletId;
        this.txnType = txnType;
        this.amount = amount;
        this.referenceCode = referenceCode;

        // Time initialization
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    // Getters and Setters

    public Long getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Long getWalletId() {
        return walletId;
    }
    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public TransactionType getTxnType() {
        return txnType;
    }
    public void setTxnType(TransactionType txnType) {
        this.txnType = txnType;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReferenceCode(){
        return referenceCode;
    }
    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    @Override
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }
    @Override
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", walletId=" + walletId +
                ", txnType=" + txnType +
                ", amount=" + amount +
                ", referenceCode='" + referenceCode + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
