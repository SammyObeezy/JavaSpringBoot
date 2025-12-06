package org.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.example.auth.utils.ValidationConstants;

@Data
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Pattern(regexp = ValidationConstants.EMAIL_REGEX, message = ValidationConstants.EMAIL_MESSAGE)
    private String email;

    @NotBlank(message = "Password is required")
    // We don't enforce strict password pattern on login (in case we change policy later, we don't want to block old users),
    // but we can enforce non-empty.
    private String password;
}