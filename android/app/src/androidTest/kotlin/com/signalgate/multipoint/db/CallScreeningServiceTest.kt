package com.signalgate.multipoint

import android.telecom.Call
import com.signalgate.multipoint.db.BlockEntry
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallScreeningServiceTest {

    // Mocked blocking logic from your service
    private fun shouldBlockCall(incomingNumber: String?, blockedList: List<BlockEntry>): Boolean {
        if (incomingNumber == null) return false
        val cleanNumber = incomingNumber.replace(Regex("[^0-9+]"), "")
        return blockedList.any { block ->
            val cleanBlock = block.phoneNumber.replace(Regex("[^0-9+]"), "")
            cleanNumber.contains(cleanBlock) || cleanBlock.contains(cleanNumber)
        }
    }

    @Test
    fun `blocks known spam number`() {
        val blocked = listOf(BlockEntry(phoneNumber = "+18005551212", reason = "spam"))
        assertTrue(shouldBlockCall("+1-800-555-1212", blocked))
    }

    @Test
    fun `does not block safe number`() {
        val blocked = listOf(BlockEntry(phoneNumber = "+18005551212", reason = "spam"))
        assertFalse(shouldBlockCall("+13105551212", blocked))
    }

    @Test
    fun `handles null number gracefully`() {
        val blocked = listOf<BlockEntry>()
        assertFalse(shouldBlockCall(null, blocked))
    }
}
