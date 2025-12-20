package org.example.escrow.dto.identity;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.example.escrow.model.enums.UserRole;
import org.example.escrow.util.ValidationUtils;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = ValidationUtils.PASSWORD_REGEX, message = "Invalid number format. Use Use 2547XXXXXXXX")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = ValidationUtils.PASSWORD_REGEX, message = "Password must be at least 8 chars, contain digit, special char, upper and lower case")
    private String password;

    // Optional: Defaults to USER if null
    private UserRole role;

    // Optional: Merchant details if role is MERCHANT
    private String businessName;
}
