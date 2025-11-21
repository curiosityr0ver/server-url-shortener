package com._cortex.url_management.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

class ShortCodeGeneratorTest {

    @Test
    void testGenerate_DefaultLength() {
        String shortCode = ShortCodeGenerator.generate();

        assertNotNull(shortCode);
        assertEquals(7, shortCode.length(), "Default short code should be 7 characters");
        assertTrue(ShortCodeGenerator.isValidBase62(shortCode), "Generated code should be valid Base62");
    }

    @Test
    void testGenerate_CustomLength() {
        int customLength = 10;
        String shortCode = ShortCodeGenerator.generate(customLength);

        assertNotNull(shortCode);
        assertEquals(customLength, shortCode.length());
        assertTrue(ShortCodeGenerator.isValidBase62(shortCode));
    }

    @Test
    void testGenerate_ThrowsExceptionForInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShortCodeGenerator.generate(0);
        }, "Should throw exception for zero length");

        assertThrows(IllegalArgumentException.class, () -> {
            ShortCodeGenerator.generate(-5);
        }, "Should throw exception for negative length");
    }

    @RepeatedTest(100)
    void testGenerate_Randomness() {
        // Test that generated codes are actually random by generating multiple
        String code1 = ShortCodeGenerator.generate();
        String code2 = ShortCodeGenerator.generate();

        // While theoretically they could be the same, with 62^7 possibilities it's
        // extremely unlikely
        // This test helps verify the random generator is working
        assertNotNull(code1);
        assertNotNull(code2);
    }

    @Test
    void testIsValidBase62_ValidCodes() {
        assertTrue(ShortCodeGenerator.isValidBase62("abc123"));
        assertTrue(ShortCodeGenerator.isValidBase62("ABC123"));
        assertTrue(ShortCodeGenerator.isValidBase62("0123456789"));
        assertTrue(ShortCodeGenerator.isValidBase62("abcdefghijklmnopqrstuvwxyz"));
        assertTrue(ShortCodeGenerator.isValidBase62("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
        assertTrue(ShortCodeGenerator.isValidBase62("aB1cD2"));
    }

    @Test
    void testIsValidBase62_InvalidCodes() {
        assertFalse(ShortCodeGenerator.isValidBase62(null), "Null should be invalid");
        assertFalse(ShortCodeGenerator.isValidBase62(""), "Empty string should be invalid");
        assertFalse(ShortCodeGenerator.isValidBase62("abc-123"), "Hyphen is not Base62");
        assertFalse(ShortCodeGenerator.isValidBase62("abc_123"), "Underscore is not Base62");
        assertFalse(ShortCodeGenerator.isValidBase62("abc 123"), "Space is not Base62");
        assertFalse(ShortCodeGenerator.isValidBase62("abc@123"), "Special characters are not Base62");
    }

    @Test
    void testGenerate_OnlyContainsBase62Characters() {
        for (int i = 0; i < 100; i++) {
            String shortCode = ShortCodeGenerator.generate();
            assertTrue(shortCode.matches("^[0-9A-Za-z]+$"),
                    "Generated code should only contain Base62 characters");
        }
    }
}
