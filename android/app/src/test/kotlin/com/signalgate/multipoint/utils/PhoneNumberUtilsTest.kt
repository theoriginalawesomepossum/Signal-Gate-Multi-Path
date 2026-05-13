package com.signalgate.multipoint.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberUtilsTest {

    // ─── normalizePhoneNumber ─────────────────────────────────────────────────

    @Test
    fun `normalizePhoneNumber removes formatting correctly`() {
        assertEquals("+18005551212",  PhoneNumberUtils.normalizePhoneNumber("+1-800-555-1212"))
        assertEquals("3105551212",    PhoneNumberUtils.normalizePhoneNumber("(310) 555-1212"))
        assertEquals("18005551212",   PhoneNumberUtils.normalizePhoneNumber("1.800.555.1212"))
        assertEquals("+15551234567",  PhoneNumberUtils.normalizePhoneNumber("+1 (555) 123-4567"))
        assertEquals("",              PhoneNumberUtils.normalizePhoneNumber(""))
        assertEquals("+123",          PhoneNumberUtils.normalizePhoneNumber("+1-2-3"))
    }

    // ─── formatPhoneNumberForDisplay ─────────────────────────────────────────

    @Test
    fun `formatPhoneNumberForDisplay formats bare 10-digit numbers`() {
        assertEquals("(310) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("3105551212"))
        assertEquals("(800) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("8005551212"))
    }

    @Test
    fun `formatPhoneNumberForDisplay formats plus-prefixed 11-digit numbers with country code`() {
        assertEquals("+1 (800) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("+18005551212"))
        assertEquals("+1 (555) 123-4567", PhoneNumberUtils.formatPhoneNumberForDisplay("+15551234567"))
        assertEquals("+1 (234) 567-8901", PhoneNumberUtils.formatPhoneNumberForDisplay("+12345678901"))
    }

    @Test
    fun `formatPhoneNumberForDisplay strips leading 1 from unprefixed 11-digit numbers`() {
        // No '+' → treat leading digit as country code, drop it, format remainder
        assertEquals("(800) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("18005551212"))
    }

    @Test
    fun `formatPhoneNumberForDisplay returns original string for unrecognized formats`() {
        assertEquals("123",   PhoneNumberUtils.formatPhoneNumberForDisplay("123"))
        assertEquals("abc",   PhoneNumberUtils.formatPhoneNumberForDisplay("abc"))
        assertEquals("",      PhoneNumberUtils.formatPhoneNumberForDisplay(""))
    }
}
