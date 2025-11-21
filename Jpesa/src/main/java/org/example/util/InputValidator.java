package org.example.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    // Regex: Optional (+254 or 254 or 0) followed by (7 or 1 and 8 digits)
    // Examples: 0712345678, 254712345678, +254712345678
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?254|0)?([71]\\d{8})$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validates and formats phone number to standard 2547XXXXXXXX format.
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null) throw new IllegalArgumentException("Phone number cannot be null");

        // AGGRESSIVE CLEANING: Remove anything that is NOT a digit or a plus sign.
        // This fixes issues with hidden spaces, dashes, or weird copy-paste characters.
        String cleanPhone = phone.replaceAll("[^0-9+]", "");

        Matcher matcher = PHONE_PATTERN.matcher(cleanPhone);
        if (matcher.matches()) {
            // Group 2 is the '712345678' part (The 9 digits)
            return "254" + matcher.group(2);
        }

        // Debugging tip: Print what we actually tried to match
        System.err.println("Validation Failed for raw: '" + phone + "' cleaned: '" + cleanPhone + "'");

        throw new IllegalArgumentException("Invalid Kenyan phone number format: " + phone);
    }
}