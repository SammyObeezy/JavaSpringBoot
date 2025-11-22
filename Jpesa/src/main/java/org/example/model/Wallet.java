package org.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Wallet implements TimeTracked {

    private Long walletId;
    private Long userId;
    private BigDecimal balance;
    private String currency;

    //Field required by TimeTracked interface
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public Wallet() {
    }

    public Wallet(Long userId){
        this.userId = userId;
        this.balance = BigDecimal.ZERO;
        this.currency = "KES";

        // Time initialization
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getWalletId(){
        return walletId;
    }
    public void setWalletId(Long walletId){
        this.walletId = walletId;
    }

    public Long getUserId(){
        return userId;
    }
    public void setUserId(Long userId){
        this.userId = userId;
    }

    public BigDecimal getBalance(){
        return balance;
    }
    public void setBalance(BigDecimal balance){
        this.balance = balance;
    }

    public String getCurrency(){
        return currency;
    }
    public void setCurrency(String currency){
        this.currency = currency;
    }

    // TimeTracked Implementation
    @Override
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    @Override
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    @Override
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString(){
        return "Wallet{" +
                "walletId=" + walletId +
                ", userId=" + userId +
                ", balance=" + balance +
                ", currency=" + currency + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}
