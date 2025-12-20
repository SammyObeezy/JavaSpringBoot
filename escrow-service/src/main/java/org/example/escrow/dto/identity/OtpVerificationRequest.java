package org.example.escrow.dto.identity;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtpVerificationRequest {

    @NotBlank(message = "User ID is required")
    private String userId; // UUID passed as String

    @NotBlank(message = "OTP Code is required")
    @Size(min = 6, max = 6, message = "OPT must be exactly 6 digits")
    private String code;
}
