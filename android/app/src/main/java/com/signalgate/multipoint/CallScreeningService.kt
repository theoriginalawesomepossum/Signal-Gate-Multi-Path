package com.signalgate.multipoint

import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import org.koin.android.ext.android.inject
import com.signalgate.multipoint.logic.CallScreeningEngine
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * CallScreeningService handles incoming calls and determines whether to allow, block, or screen them.
 * It implements the Priority Hierarchy:
 * 1. Manual Allow-list (Whitelist)
 * 2. Manual Block-list
 * 3. Pattern/Prefix Rules
 * 4. Aggregated Data Sources
 * 5. Default (Allow)
 */
class CallScreeningService : CallScreeningService() {
    private val screeningEngine: CallScreeningEngine by inject()

    companion object {
        private const val TAG = "SignalGateCallScreening"
    }

    /**
     * Enum representing the decision for an incoming call.
     */
    enum class CallDecision {
        ALLOW,
        BLOCK,
        SCREEN
    }

    override fun onScreenCall(details: Call.Details) {
        Log.d(TAG, "Screening call from: ${details.handle?.schemeSpecificPart}")

        // Extract phone number from the incoming call
        val phoneNumber = details.handle?.schemeSpecificPart ?: run {
            super.onScreenCall(details)
            return
        }

        // Process the call screening asynchronously
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val callInfo = analyzeIncomingCall(phoneNumber)
                val decision = determineCallDecision(callInfo)

                // Apply the decision
                applyCallDecision(details, decision)

                // Trigger the overlay if the call is likely spam
                if (callInfo.spamStatus == "LIKELY SPAM" || callInfo.spamStatus == "SPAM") {
                    triggerOverlay(callInfo)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error screening call", e)
                // Default to allowing the call on error
                respondToCall(details, CallDecision.ALLOW)
            }
        }
    }

    /**
     * Analyzes an incoming call and returns a CallInfo object with spam status, confidence, etc.
     */
    private suspend fun analyzeIncomingCall(phoneNumber: String): CallInfo {
        // Use the screening engine to analyze the call
        return screeningEngine.screenCall(phoneNumber)
    }

    /**
     * Determines the call decision based on the Priority Hierarchy.
     */
    private fun determineCallDecision(callInfo: CallInfo): CallDecision {
        return callInfo.callDecision
    }

    /**
     * Applies the call decision by responding to the call.
     */
    private fun applyCallDecision(details: Call.Details, decision: CallDecision) {
        respondToCall(details, decision)
    }

    /**
     * Responds to the call with the specified decision.
     */
    private fun respondToCall(details: Call.Details, decision: CallDecision) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val response = when (decision) {
            CallDecision.ALLOW -> {
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
            }
            CallDecision.BLOCK -> {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setSkipCallLog(true)
                    .setSkipNotification(true)
                    .build()
            }
            CallDecision.SCREEN -> {
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
            }
        }

        super.respondToCall(details, response)
    }

    /**
     * Triggers the overlay service to display the incoming call shield.
     */
    private fun triggerOverlay(callInfo: CallInfo) {
        val overlayIntent = Intent(this, CallOverlayService::class.java).apply {
            putExtra("call_info", callInfo)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(overlayIntent)
        } else {
            startService(overlayIntent)
        }
    }

    /**
     * Normalizes a phone number for consistent matching.
     */
    private fun normalizePhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except leading +
        return phoneNumber.replace(Regex("[^0-9+]"), "")
    }
}
