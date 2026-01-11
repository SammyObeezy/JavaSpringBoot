package org.example.escrow.dto.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.escrow.model.enums.NotificationChannel;

@Data
public class ResendOtpRequest {

    @Email( message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    private NotificationChannel channel;
}
