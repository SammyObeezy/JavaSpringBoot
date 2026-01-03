package org.example.escrow.dto.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.util.ValidationUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = ValidationUtils.PHONE_REGEX, message = "Invalid phone format. Use 07xx, 01xx, or 254xx")
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Pattern(regexp = ValidationUtils.PASSWORD_REGEX, message = "Password must be 8+ chars, with digit, upper, lower, & special char")
    private String password;

    private UserRole role;
}