package com.signalgate.multipoint.repository

import com.signalgate.multipoint.db.PhoneEntry
import com.signalgate.multipoint.db.PhoneEntryDao
import com.signalgate.multipoint.db.SourceDao
import javax.inject.Inject

class DataSourceRepository @Inject constructor(
    private val phoneDao: PhoneEntryDao,
    private val sourceDao: SourceDao
) {

    private fun normalizePhoneNumber(raw: String): String {
        if (raw.isBlank()) return ""
        
        // Robust normalization
        var cleaned = raw.replace(Regex("[^0-9+\\s]"), "").trim()
        if (cleaned.startsWith("1") && cleaned.length == 11) {
            cleaned = "+$cleaned"
        } else if (!cleaned.startsWith("+")) {
            cleaned = "+1$cleaned"
        }
        return cleaned
    }

    suspend fun getCallDecision(rawNumber: String): CallDecision {
        val normalized = normalizePhoneNumber(rawNumber)
        if (normalized.isBlank()) {
            return CallDecision(PhoneEntry.ActionType.ALLOW, "Invalid number", 0, "default")
        }

        // 1. Manual Allow (Absolute Highest Priority)
        phoneDao.findByNumberAndAction(normalized, PhoneEntry.ActionType.ALLOW)?.let {
            return CallDecision(PhoneEntry.ActionType.ALLOW, "Manual Allow List", it.confidence, "manual_allow")
        }

        // 2. Manual Block
        phoneDao.findByNumberAndAction(normalized, PhoneEntry.ActionType.BLOCK)?.let {
            return CallDecision(PhoneEntry.ActionType.BLOCK, "Manual Block List", it.confidence, "manual_block")
        }

        // 3. Pattern Rules
        phoneDao.findMatchingPattern(normalized)?.let {
            return CallDecision(PhoneEntry.ActionType.BLOCK, "Pattern: ${it.phoneNumber}", it.confidence, "pattern")
        }

        // 4. Aggregated Sources
        phoneDao.findInEnabledSources(normalized)?.let {
            return CallDecision(PhoneEntry.ActionType.BLOCK, it.metadata ?: "External Source", it.confidence, "aggregated")
        }

        // 5. Default
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
