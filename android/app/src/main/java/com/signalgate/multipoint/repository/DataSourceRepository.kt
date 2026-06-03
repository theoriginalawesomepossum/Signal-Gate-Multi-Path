package com.signalgate.multipoint.repository

import com.signalgate.multipoint.db.PhoneEntry
import com.signalgate.multipoint.db.PhoneEntryDao
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DataSourceRepository : KoinComponent {

    private val phoneDao: PhoneEntryDao by inject()

    private fun normalizePhoneNumber(raw: String): String {
        if (raw.isBlank()) return ""
        var cleaned = raw.replace(Regex("[^0-9+\\s]"), "").trim()
        if (cleaned.startsWith("1") && cleaned.length == 11) {
            cleaned = "+$cleaned"
        } else if (!cleaned.startsWith("+") && cleaned.length >= 10) {
            cleaned = "+1$cleaned"
        }
        return cleaned
    }

    suspend fun getCallDecision(rawNumber: String): CallDecision {
        val normalized = normalizePhoneNumber(rawNumber)
        if (normalized.isBlank()) {
            return CallDecision(PhoneEntry.ActionType.ALLOW, "Invalid number", 0, "default")
        }

        phoneDao.findByNumberAndAction(normalized, PhoneEntry.ActionType.ALLOW)?.let {
            return CallDecision(PhoneEntry.ActionType.ALLOW, "Manual Allow List", it.confidence, "manual_allow")
        }

        phoneDao.findByNumberAndAction(normalized, PhoneEntry.ActionType.BLOCK)?.let {
            return CallDecision(PhoneEntry.ActionType.BLOCK, "Manual Block List", it.confidence, "manual_block")
        }

        phoneDao.findMatchingPattern(normalized)?.let {
            return CallDecision(PhoneEntry.ActionType.BLOCK, "Pattern: ${it.phoneNumber}", it.confidence, "pattern")
        }

        phoneDao.findInEnabledSources(normalized)?.let {
            return CallDecision(PhoneEntry.ActionType.BLOCK, it.metadata ?: "External Source", it.confidence, "aggregated")
        }

        return CallDecision(PhoneEntry.ActionType.ALLOW, "No rule matched", 0, "default")
    }

    suspend fun insertEntry(entry: PhoneEntry) {
        val sanitized = entry.copy(phoneNumber = normalizePhoneNumber(entry.phoneNumber))
        phoneDao.insert(sanitized)
    }

    data class CallDecision(
        val action: PhoneEntry.ActionType,
        val reason: String,
        val confidence: Int,
        val source: String
    )
}
