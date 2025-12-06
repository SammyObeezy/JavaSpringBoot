package org.example.auth.utils;

public class ValidationConstants {

    private ValidationConstants(){}

    public static final String KENYAN_PHONE_REGEX =  "^(\\+254|254|0)((7|1)[0-9]{8})$";

    public static final String NAME_REGEX = "^[a-zA-Z\\s'-]{2,50}$";

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    public static final String PHONE_MESSAGE = "Invalid phone number. Must be a valid Kenyan format (e.g., 0712345678, +2547...).";
    public static final String NAME_MESSAGE = "Name must contain only letters, spaces, hyphens, or apostrophes (2-50 characters).";
    public static final String EMAIL_MESSAGE = "Invalid email address format.";
    public static final String PASSWORD_MESSAGE = "Password must be at least 8 characters long, containing 1 uppercase, 1 lowercase, 1 digit, and 1 special character.";
    public static final String REQUIRED_MESSAGE = "This field is required.";
}
