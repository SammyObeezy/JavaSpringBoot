package org.example.dto;

import java.math.BigDecimal;

public class TransactionRequest {
    private String phoneNumber; // Who is doing the transaction
    private BigDecimal amount;

    public TransactionRequest() {

    }

    public TransactionRequest(String phoneNumber, BigDecimal amount){
        this.phoneNumber = phoneNumber;
        this.amount = amount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
