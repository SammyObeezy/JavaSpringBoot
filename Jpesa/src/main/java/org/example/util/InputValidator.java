package org.example.util;

import java.util.regex.Pattern;

public class InputValidator {

    // Regex for Kenyan Phone Numbers (Accepts +254, 254, 07, 01 prefixes
    // It ensures the result is always 12 digits starting with 254
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:254|\\\\+254|0)?([71]\\\\d{8})$");

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    // Min 8 chars, at leat 1 letter and 1 number
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\\\d)[A-Za-z\\\\d@$!%*#?&]{8,}$");

    public static boolean isValidEmail(String email){
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password){
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
    /*
    *Validates and formats phone number to standard 2547XXXXXXX format
    * Throws IllegalArgumentException if invalid.
     */

    public static String formatPhoneNumber(String phone){
        if (phone == null) throw new IllegalArgumentException("Phone number cannot be null");

        // Remove spaces or dashes
        String cleanPhone = phone.replaceAll("\\s+", "").replaceAll("-", "");

        var matcher = PHONE_PATTERN.matcher(cleanPhone);
        if(matcher.matches()){
            // Group 1 captures the last 9 digits (e.g./ 712345678
            return "254" + matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid Kenyan phone number format: " + phone);
    }
}
