package com.signalgate.multipoint.repository

import com.signalgate.multipoint.database.daos.UnifiedEntryDao
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity

class DataSourceRepository(
    private val unifiedEntryDao: UnifiedEntryDao
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
            return CallDecision("ALLOW", "Invalid number", 0, "default")
        }

        // 1. Manual Allow (Absolute Highest Priority)
        unifiedEntryDao.findAllowEntry(normalized)?.let {
            return CallDecision("ALLOW", "Manual Allow List", it.confidence, "manual_allow")
        }

        // 2. Manual Block
        unifiedEntryDao.findBlockEntry(normalized)?.let {
            return CallDecision("BLOCK", "Manual Block List", it.confidence, "manual_block")
        }

        // 3. Pattern Rules
        // Note: The DAO currently doesn't have a direct pattern matching query for a specific number, 
        // but it has getAllBlockPatterns(). For now, we'll keep the logic simple or use a placeholder.
        val patterns = unifiedEntryDao.getAllBlockPatterns()
        patterns.find { normalized.startsWith(it.phoneNumber) }?.let {
            return CallDecision("BLOCK", "Pattern: ${it.phoneNumber}", it.confidence, "pattern")
        }

        // 4. Aggregated Sources
        // Logic depends on how 'enabled sources' are handled. For now, check if any entry exists.
        unifiedEntryDao.findEntriesByPhoneNumber(normalized).firstOrNull { it.action == "BLOCK" }?.let {
            return CallDecision("BLOCK", it.metadata ?: "External Source", it.confidence, "aggregated")
        }

        // 5. Default
        return CallDecision("ALLOW", "No rule matched", 0, "default")
    }

    suspend fun insertEntry(entry: UnifiedEntryEntity) {
        val sanitized = entry.copy(phoneNumber = normalizePhoneNumber(entry.phoneNumber))
        unifiedEntryDao.insertEntry(sanitized)
    }

    data class CallDecision(
        val action: String,
        val reason: String,
        val confidence: Int,
        val source: String
    )
}
