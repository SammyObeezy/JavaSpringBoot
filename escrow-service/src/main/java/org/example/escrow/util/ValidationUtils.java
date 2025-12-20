package org.example.escrow.util;

import java.util.regex.Pattern;

public final class ValidationUtils {

    // Prevent instantiation
    private ValidationUtils() {}

    /**
     * Regex for Kenyan Phone Numbers.
     * Accepted formats:
     * - +2547XXXXXXXX
     * - 2547XXXXXXXX
     * - 07XXXXXXXX
     * - 01XXXXXXXX
     */
    public static final String PHONE_REGEX = "^(?:254|\\+254|0)?([71][0-9]{8})$";

    /**
     * Regex for strong passwords.
     * - At least 8 characters
     * - At least one digit
     * - At least one uppercase letter
     * - At least one special char (@#$%^&+=)
     */
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";

    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    /**
     * Helper method to manually validate a phone number if needed outside of DTOs.
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && Pattern.matches(PHONE_REGEX, phoneNumber);
    }

    /**
     * Helper to sanitize phone numbers to a standard format (2547...).
     * This ensures strict uniqueness in the database.
     */
    public static String sanitizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        // Remove spaces or hyphens
        String sanitized = phoneNumber.replaceAll("\\s+", "").replaceAll("-", "");

        // Convert 07... or 01... to 2547...
        if (sanitized.startsWith("0")) {
            return "254" + sanitized.substring(1);
        }
        // Convert +254... to 254...
        if (sanitized.startsWith("+")) {
            return sanitized.substring(1);
        }

        return sanitized;
    }
}