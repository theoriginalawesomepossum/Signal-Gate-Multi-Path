package com.signalgate.multipoint.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberUtilsTest {

    @Test
    fun `normalizePhoneNumber removes formatting correctly`() {
        try {
            assertEquals("+18005551212", PhoneNumberUtils.normalizePhoneNumber("+1-800-555-1212"))
            assertEquals("3105551212", PhoneNumberUtils.normalizePhoneNumber("(310) 555-1212"))
            assertEquals("18005551212", PhoneNumberUtils.normalizePhoneNumber("1.800.555.1212"))
            assertEquals("+15551234567", PhoneNumberUtils.normalizePhoneNumber("+1 (555) 123-4567"))
            
            assertEquals("", PhoneNumberUtils.normalizePhoneNumber(""))
            assertEquals("+123", PhoneNumberUtils.normalizePhoneNumber("+1-2-3"))
            
            println("✅ normalizePhoneNumber tests passed")
        } catch (e: Exception) {
            throw AssertionError("normalizePhoneNumber test failed", e)
        }
    }

    @Test
    fun `formatPhoneNumberForDisplay works correctly`() {
        try {
            // 10-digit US numbers
            assertEquals("(800) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("+18005551212"))
            assertEquals("(310) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("3105551212"))
            assertEquals("(555) 123-4567", PhoneNumberUtils.formatPhoneNumberForDisplay("+15551234567"))
            
            // 11-digit international number
            val result = PhoneNumberUtils.formatPhoneNumberForDisplay("+12345678901")
            assertEquals("+1 (234) 567-8901", result)
            
            // Fallback cases
            assertEquals("123", PhoneNumberUtils.formatPhoneNumberForDisplay("123"))
            assertEquals("", PhoneNumberUtils.formatPhoneNumberForDisplay(""))
            assertEquals("abc", PhoneNumberUtils.formatPhoneNumberForDisplay("abc"))
            
            println("✅ formatPhoneNumberForDisplay tests passed")
        } catch (e: Exception) {
            throw AssertionError("formatPhoneNumberForDisplay test failed", e)
        }
    }
}
