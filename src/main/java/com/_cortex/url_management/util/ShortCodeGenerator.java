package com._cortex.url_management.util;

import java.security.SecureRandom;

/**
 * Utility class for generating short codes using Base62 encoding
 */
public class ShortCodeGenerator {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEFAULT_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ShortCodeGenerator() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generate a random Base62 short code with default length (7 characters)
     * 
     * @return random Base62 string
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Generate a random Base62 short code with specified length
     * 
     * @param length the length of the short code
     * @return random Base62 string
     */
    public static String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62_CHARS.charAt(RANDOM.nextInt(BASE62_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Validate if a string is a valid Base62 short code
     * 
     * @param shortCode the short code to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBase62(String shortCode) {
        if (shortCode == null || shortCode.isEmpty()) {
            return false;
        }

        for (char c : shortCode.toCharArray()) {
            if (BASE62_CHARS.indexOf(c) == -1) {
                return false;
            }
        }
        return true;
    }
}
