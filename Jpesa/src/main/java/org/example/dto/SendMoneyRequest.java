package org.example.dto;

import java.math.BigDecimal;

public class SendMoneyRequest {
    private String senderPhone;
    private String recipientPhone;
    private BigDecimal amount;

    public SendMoneyRequest() {}

    public SendMoneyRequest(String senderPhone, String recipientPhone, BigDecimal amount) {
        this.senderPhone = senderPhone;
        this.recipientPhone = recipientPhone;
        this.amount = amount;
    }

    public String getSenderPhone() { return senderPhone; }
    public void setSenderPhone(String senderPhone) { this.senderPhone = senderPhone; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}