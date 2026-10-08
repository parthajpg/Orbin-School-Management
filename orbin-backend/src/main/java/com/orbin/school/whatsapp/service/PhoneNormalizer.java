package com.orbin.school.whatsapp.service;

import org.springframework.stereotype.Component;

/**
 * Normalizes Indian mobile phone numbers to the canonical form: +91XXXXXXXXXX
 *
 * <p>Handles formats:
 * <ul>
 *   <li>9876543210    → +919876543210</li>
 *   <li>09876543210   → +919876543210</li>
 *   <li>+919876543210 → +919876543210</li>
 *   <li>91 9876543210 → +919876543210</li>
 *   <li>WhatsApp format: 919876543210@ → +919876543210</li>
 * </ul>
 */
@Component
public class PhoneNormalizer {

    private static final String COUNTRY_CODE = "91";
    private static final int    MOBILE_LENGTH = 10;

    public static String normalize(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Remove WhatsApp suffix (@s.whatsapp.net or @c.us)
        String phone = rawPhone.split("@")[0];

        // Remove all non-digit characters
        phone = phone.replaceAll("[^0-9]", "");

        // Remove leading country code duplicates
        if (phone.startsWith("91") && phone.length() == 12) {
            phone = phone.substring(2);
        } else if (phone.startsWith("0") && phone.length() == 11) {
            phone = phone.substring(1);
        }

        if (phone.length() != MOBILE_LENGTH) {
            throw new IllegalArgumentException("Invalid mobile number: " + rawPhone);
        }

        return "+" + COUNTRY_CODE + phone;
    }

    /**
     * Normalizes without throwing — returns null for invalid input.
     */
    public static String normalizeOrNull(String rawPhone) {
        try {
            return normalize(rawPhone);
        } catch (Exception e) {
            return null;
        }
    }
}
