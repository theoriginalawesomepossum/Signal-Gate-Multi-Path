package com.signalgate.multipoint.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneNumberUtilsTest {

    // Example blocking logic (adapt if your real PhoneNumberUtils.kt has different methods)
    private fun isBlocked(number: String, blockedPrefixes: List<String>): Boolean {
        val cleanNumber = number.replace(Regex("[^0-9+]"), "")
        return blockedPrefixes.any { prefix ->
            cleanNumber.startsWith(prefix.replace(Regex("[^0-9+]"), ""))
        }
    }

    @Test
    fun `blocks exact number match`() {
        val blocked = listOf("+18005551212")
        assertTrue(isBlocked("+1-800-555-1212", blocked))
        assertTrue(isBlocked("18005551212", blocked))
    }

    @Test
    fun `blocks prefix match`() {
        val blocked = listOf("1800")
        assertTrue(isBlocked("+18005551212", blocked))
        assertTrue(isBlocked("18005551212", blocked))
    }

    @Test
    fun `does not block safe numbers`() {
        val blocked = listOf("1800")
        assertFalse(isBlocked("+13105551212", blocked))
        assertFalse(isBlocked("911", blocked))
    }
}
