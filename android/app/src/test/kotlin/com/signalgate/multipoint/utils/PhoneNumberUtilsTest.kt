package com.signalgate.multipoint.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberUtilsTest {

    @Test
    fun `normalizePhoneNumber removes formatting correctly`() {
        try {
            // Core test cases
            assertEquals("+18005551212", PhoneNumberUtils.normalizePhoneNumber("+1-800-555-1212"))
            assertEquals("3105551212", PhoneNumberUtils.normalizePhoneNumber("(310) 555-1212"))
            assertEquals("18005551212", PhoneNumberUtils.normalizePhoneNumber("1.800.555.1212"))
            assertEquals("+15551234567", PhoneNumberUtils.normalizePhoneNumber("+1 (555) 123-4567"))
            
            // Edge cases
            assertEquals("", PhoneNumberUtils.normalizePhoneNumber(""))
            assertEquals("+123", PhoneNumberUtils.normalizePhoneNumber("+1-2-3"))
            
            println("✅ normalizePhoneNumber tests passed")
        } catch (e: Exception) {
            throw AssertionError("normalizePhoneNumber test failed", e)
        }
    }

    @Test
    fun `formatPhoneNumberForDisplay works for US numbers`() {
        try {
            assertEquals("+1 (800) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("+18005551212"))
            assertEquals("(310) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("3105551212"))
            assertEquals("+1 (555) 123-4567", PhoneNumberUtils.formatPhoneNumberForDisplay("+15551234567"))
            
            // Edge cases
            assertEquals("Unknown", PhoneNumberUtils.formatPhoneNumberForDisplay(""))
            assertEquals("123", PhoneNumberUtils.formatPhoneNumberForDisplay("123"))
            
            println("✅ formatPhoneNumberForDisplay tests passed")
        } catch (e: Exception) {
            throw AssertionError("formatPhoneNumberForDisplay test failed", e)
        }
    }

    @Test
    fun `handles empty input gracefully`() {
        try {
            // We can't pass null if the function doesn't accept String?, so we test empty string
            assertEquals("", PhoneNumberUtils.normalizePhoneNumber(""))
            assertEquals("Unknown", PhoneNumberUtils.formatPhoneNumberForDisplay(""))
            println("✅ Empty input handling test passed")
        } catch (e: Exception) {
            throw AssertionError("Empty input handling test failed", e)
        }
    }
}
