package org.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.example.auth.utils.ValidationConstants;

@Data
public class RegisterRequest {

    @NotBlank(message = ValidationConstants.REQUIRED_MESSAGE)
    @Pattern(regexp = ValidationConstants.NAME_REGEX, message = "First " + ValidationConstants.NAME_MESSAGE)
    private String firstName;

    @NotBlank(message = ValidationConstants.REQUIRED_MESSAGE)
    @Pattern(regexp = ValidationConstants.NAME_REGEX, message = "Last " + ValidationConstants.NAME_MESSAGE)
    private String lastName;

    @NotBlank(message = ValidationConstants.REQUIRED_MESSAGE)
    @Pattern(regexp = ValidationConstants.EMAIL_REGEX, message = ValidationConstants.EMAIL_MESSAGE)
    private String email;

    @NotBlank(message = ValidationConstants.REQUIRED_MESSAGE)
    @Pattern(regexp = ValidationConstants.KENYAN_PHONE_REGEX, message = ValidationConstants.PHONE_MESSAGE)
    private String phoneNumber;

    @NotBlank(message = ValidationConstants.REQUIRED_MESSAGE)
    @Pattern(regexp = ValidationConstants.PASSWORD_REGEX, message = ValidationConstants.PASSWORD_MESSAGE)
    private String password;


}
