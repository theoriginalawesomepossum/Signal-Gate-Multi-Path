package com.signalgate.multipoint.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberUtilsTest {

    @Test
    fun `normalizePhoneNumber removes formatting correctly`() {
        assertEquals("+18005551212", PhoneNumberUtils.normalizePhoneNumber("+1-800-555-1212"))
        assertEquals("+13105551212", PhoneNumberUtils.normalizePhoneNumber("(310) 555-1212"))
        assertEquals("18005551212", PhoneNumberUtils.normalizePhoneNumber("1.800.555.1212"))
    }

    @Test
    fun `formatPhoneNumberForDisplay works for US numbers`() {
        // +18005551212 becomes 11 digits → international formatting
        assertEquals("+1 (800) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("+18005551212"))
        // 10-digit US number
        assertEquals("(310) 555-1212", PhoneNumberUtils.formatPhoneNumberForDisplay("3105551212"))
    }
}
