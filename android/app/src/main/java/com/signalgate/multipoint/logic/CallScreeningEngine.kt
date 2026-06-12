package com.signalgate.multipoint.logic

import android.util.Log
import com.signalgate.multipoint.CallInfo
import com.signalgate.multipoint.SignalGateCallScreeningService
import com.signalgate.multipoint.database.repositories.DataSourceRepository

/**
 * CallScreeningEngine implements the Priority Hierarchy for call screening.
 * Uses [DataSourceRepository] as the single production data path — no direct
 * database or DAO access.
 *
 * Priority order:
 * 1. Manual Allow-list (Whitelist)
 * 2. Manual Block-list
 * 3. Pattern/Prefix Rules
 * 4. Aggregated Data Sources
 * 5. Default (Allow)
 */
class CallScreeningEngine(private val repository: DataSourceRepository) {

    companion object {
        private const val TAG = "CallScreeningEngine"
        private const val ACTION_BLOCK = "BLOCK"
    }

    /**
     * Screens an incoming call and returns a [CallInfo] object with the decision.
     */
    suspend fun screenCall(phoneNumber: String): CallInfo {
        val normalizedNumber = normalizePhoneNumber(phoneNumber)
        Log.d(TAG, "Screening call from: $phoneNumber (normalized: $normalizedNumber)")

        // Delegate the full priority-hierarchy lookup to the repository.
        val decision = repository.getCallDecision(normalizedNumber)

        return when (decision.action) {
            "ALLOW" -> {
                Log.d(TAG, "Call allowed — reason: ${decision.reason}")
                CallInfo(
                    originalPhoneNumber = phoneNumber,
                    normalizedPhoneNumber = normalizedNumber,
                    spamStatus = if (decision.source == "manual_allow") "SAFE" else "UNKNOWN",
                    spamCategory = null,
                    confidence = decision.confidence,
                    riskLevel = "LOW",
                    matchedSources = listOf(decision.reason),
                    callDecision = SignalGateCallScreeningService.CallDecision.ALLOW
                )
            }
            "BLOCK" -> {
                Log.d(TAG, "Call blocked — reason: ${decision.reason}")
                val isHighConfidence = decision.confidence >= 70
                CallInfo(
                    originalPhoneNumber = phoneNumber,
                    normalizedPhoneNumber = normalizedNumber,
                    spamStatus = if (decision.source == "pattern") "BLOCKED" else "LIKELY SPAM",
                    spamCategory = null,
                    confidence = decision.confidence,
                    riskLevel = "HIGH",
                    matchedSources = listOf(decision.reason),
                    callDecision = if (isHighConfidence) {
                        SignalGateCallScreeningService.CallDecision.BLOCK
                    } else {
                        SignalGateCallScreeningService.CallDecision.SCREEN
                    }
                )
            }
            else -> {
                // Default: allow
                Log.d(TAG, "Call allowed by default")
                CallInfo(
                    originalPhoneNumber = phoneNumber,
                    normalizedPhoneNumber = normalizedNumber,
                    spamStatus = "UNKNOWN",
                    spamCategory = null,
                    confidence = null,
                    riskLevel = null,
                    matchedSources = emptyList(),
                    callDecision = SignalGateCallScreeningService.CallDecision.ALLOW
                )
            }
        }
    }

    /**
     * Normalizes a phone number for consistent matching.
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
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
