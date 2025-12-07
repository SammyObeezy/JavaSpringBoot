package org.example.booking.model.enums;

public enum BookingStatus {
    PENDING_PAYMENT, // Created, waiting for M-Pesa
    CONFIRMED, // Payment successful
    CANCELLED, // User cancelled or Payment failed
    FAILED     // System error
}
