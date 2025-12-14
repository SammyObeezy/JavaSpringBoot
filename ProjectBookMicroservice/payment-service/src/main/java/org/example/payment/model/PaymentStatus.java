package org.example.payment.model;

public enum PaymentStatus {
    PENDING, // Request sent to M-pesa (STK Push sent)
    COMPLETED, // Success callback received
    FAILED, // Failed callback or insufficient funds
    CANCELLED // User cancelled on phone
}
