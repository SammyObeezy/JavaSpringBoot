package org.example.escrow.dto.identity;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    // Use can login with Email OR Phone
    @NotBlank(message = "Email or Phone is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
