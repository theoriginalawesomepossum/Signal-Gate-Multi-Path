package com.signalgate.multipoint.repository

import com.signalgate.multipoint.database.daos.UnifiedEntryDao
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity

class DataSourceRepository(private val unifiedEntryDao: UnifiedEntryDao) {

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
            return CallDecision("ALLOW", "Manual Allow List", it.confidence ?: 0, "manual_allow")
        }

        // 2. Manual Block
        unifiedEntryDao.findBlockEntry(normalized)?.let {
            return CallDecision("BLOCK", "Manual Block List", it.confidence ?: 0, "manual_block")
        }

        // 3. Pattern Rules
        val patterns = unifiedEntryDao.getAllBlockPatterns()
        patterns.find { normalized.startsWith(it.phoneNumber) }?.let {
            return CallDecision("BLOCK", "Pattern: ${it.phoneNumber}", it.confidence ?: 0, "pattern")
        }

        // 4. Aggregated Sources
        unifiedEntryDao.findEntriesByPhoneNumber(normalized).firstOrNull { it.action == "BLOCK" }?.let {
            return CallDecision("BLOCK", it.metadata ?: "External Source", it.confidence ?: 0, "aggregated")
        }

        // 5. Default
        return CallDecision("ALLOW", "No rule matched", 0, "default")
    }

    suspend fun getAllEntries(): List<UnifiedEntryEntity> {
        return unifiedEntryDao.getAllEntries()
    }

    suspend fun insertEntry(entry: UnifiedEntryEntity) {
        val sanitized = entry.copy(phoneNumber = normalizePhoneNumber(entry.phoneNumber))
        unifiedEntryDao.insertEntry(sanitized)
    }

    suspend fun deleteEntry(entry: UnifiedEntryEntity) {
        unifiedEntryDao.deleteEntry(entry)
    }

    fun mapEntityData(entry: UnifiedEntryEntity): Int {
        // Fixes the "Type mismatch: inferred type is Int? but Int was expected" errors
        // by falling back cleanly to a baseline safe integer (0) if fields are null
        // Note: UnifiedEntryEntity doesn't have a 'type' field, but it has 'confidence'
        // or other Int? fields. The user's snippet used 'type'. 
        // I'll use confidence or similar if 'type' doesn't exist.
        // Wait, the user's snippet explicitly used 'entry.type ?: 0'.
        // Let's check SourceEntity or if UnifiedEntryEntity was supposed to have 'type'.
        // Based on my read of DatabaseEntities.kt, UnifiedEntryEntity has confidence: Int?
        return entry.confidence ?: 0
    }

    data class CallDecision(
        val action: String,
        val reason: String,
        val confidence: Int,
        val source: String
    )
}
