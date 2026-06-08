package com.signalgate.multipoint.logic

import android.util.Log
import com.signalgate.multipoint.CallInfo
import com.signalgate.multipoint.CallScreeningService
import com.signalgate.multipoint.database.SignalGateDatabase
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity

/**
 * CallScreeningEngine implements the Priority Hierarchy for call screening:
 * 1. Manual Allow-list (Whitelist)
 * 2. Manual Block-list
 * 3. Pattern/Prefix Rules
 * 4. Aggregated Data Sources
 * 5. Default (Allow)
 */
class CallScreeningEngine(private val database: SignalGateDatabase) {

    companion object {
        private const val TAG = "CallScreeningEngine"
        private const val SOURCE_MANUAL = "MANUAL"
        private const val ACTION_ALLOW = "ALLOW"
        private const val ACTION_BLOCK = "BLOCK"
    }

    /**
     * Screens an incoming call and returns a CallInfo object with the decision.
     */
    suspend fun screenCall(phoneNumber: String): CallInfo {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        Log.d(TAG, "Screening call from: $phoneNumber (normalized: $normalizedNumber)")

        // Priority 1: Check Manual Allow-list (Whitelist)
        val allowEntry = database.unifiedEntryDao().findUnifiedAllowEntry(normalizedNumber)
        if (allowEntry != null) {
            Log.d(TAG, "Call allowed by whitelist")
            return CallInfo(
                originalPhoneNumber = phoneNumber,
                normalizedPhoneNumber = normalizedNumber,
                spamStatus = "SAFE",
                spamCategory = null,
                confidence = 100,
                riskLevel = "LOW",
                matchedSources = listOf("Allow List"),
                callDecision = CallScreeningService.CallDecision.ALLOW
            )
        }

        // Priority 2: Check Manual Block-list
        val blockEntry = database.unifiedEntryDao().findUnifiedBlockEntry(normalizedNumber)
        if (blockEntry != null && !blockEntry.isPattern) {
            Log.d(TAG, "Call blocked by manual block list")
            return CallInfo(
                originalPhoneNumber = phoneNumber,
                normalizedPhoneNumber = normalizedNumber,
                spamStatus = "BLOCKED",
                spamCategory = blockEntry.category,
                confidence = 100,
                riskLevel = "HIGH",
                matchedSources = listOf("Manual Block List"),
                callDecision = CallScreeningService.CallDecision.BLOCK
            )
        }

        // Priority 3: Check Pattern/Prefix Rules
        val blockPatterns = database.unifiedEntryDao().getAllBlockPatterns()
        for (pattern in blockPatterns) {
            if (matchesPattern(normalizedNumber, pattern.phoneNumber)) {
                Log.d(TAG, "Call blocked by pattern rule: ${pattern.phoneNumber}")
                return CallInfo(
                    originalPhoneNumber = phoneNumber,
                    normalizedPhoneNumber = normalizedNumber,
                    spamStatus = "BLOCKED",
                    spamCategory = pattern.category,
                    confidence = 95,
                    riskLevel = "HIGH",
                    matchedSources = listOf("Pattern Rule"),
                    callDecision = CallScreeningService.CallDecision.BLOCK
                )
            }
        }

        // Priority 4: Check Aggregated Data Sources
        val entries = database.unifiedEntryDao().findEntriesByPhoneNumber(normalizedNumber)
        if (entries.isNotEmpty()) {
            val matchedSources = mutableListOf<String>()
            var highestRiskLevel = "LOW"
            var highestConfidence = 0

            for (entry in entries) {
                if (entry.action == ACTION_BLOCK) {
                    matchedSources.add(entry.sourceId.toString())
                    entry.confidence?.let { if (it > highestConfidence) highestConfidence = it }
                    entry.riskLevel?.let { risk ->
                        if (riskLevelToInt(risk) > riskLevelToInt(highestRiskLevel)) {
                            highestRiskLevel = risk
                        }
                    }
                }
            }

            if (matchedSources.isNotEmpty()) {
                Log.d(TAG, "Call matched in ${matchedSources.size} sources")
                return CallInfo(
                    originalPhoneNumber = phoneNumber,
                    normalizedPhoneNumber = normalizedNumber,
                    spamStatus = "LIKELY SPAM",
                    spamCategory = entries.firstOrNull()?.category,
                    confidence = highestConfidence,
                    riskLevel = highestRiskLevel,
                    matchedSources = matchedSources,
                    callDecision = if (highestConfidence >= 70) {
                        CallScreeningService.CallDecision.BLOCK
                    } else {
                        CallScreeningService.CallDecision.SCREEN
                    }
                )
            }
        }

        // Priority 5: Default to Allow
        Log.d(TAG, "Call allowed by default")
        return CallInfo(
            originalPhoneNumber = phoneNumber,
            normalizedPhoneNumber = normalizedNumber,
            spamStatus = "UNKNOWN",
            spamCategory = null,
            confidence = null,
            riskLevel = null,
            matchedSources = emptyList(),
            callDecision = CallScreeningService.CallDecision.ALLOW
        )
    }

    /**
     * Checks if a phone number matches a pattern (e.g., +1800*).
     */
    private fun matchesPattern(phoneNumber: String, pattern: String): Boolean {
        if (!pattern.contains("*")) {
            return phoneNumber == pattern
        }

        val regexPattern = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
        return phoneNumber.matches(Regex(regexPattern))
    }

    /**
     * Normalizes a phone number for consistent matching.
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except leading +
        return phoneNumber.replace(Regex("[^0-9+]"), "")
    }

    /**
     * Converts risk level string to integer for comparison.
     */
    private fun riskLevelToInt(riskLevel: String): Int {
        return when (riskLevel) {
            "LOW" -> 1
            "MEDIUM" -> 2
            "HIGH" -> 3
            else -> 0
        }
    }
}
