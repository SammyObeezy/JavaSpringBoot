package org.example.escrow.dto.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.example.escrow.util.ValidationUtils;

@Data
public class LoginRequest {

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = ValidationUtils.PHONE_REGEX, message = "Invalid phone format. Use 07xx, 01xx, or 254xx")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String password;
}