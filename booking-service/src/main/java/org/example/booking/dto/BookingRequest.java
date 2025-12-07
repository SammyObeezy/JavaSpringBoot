package org.example.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "User ID is required")
    private Long userId; // In a real app, we'd extract this from the JWT Token

    @Min(value = 1, message = "Must book at least 1 ticket")
    private int ticketCount;
}
