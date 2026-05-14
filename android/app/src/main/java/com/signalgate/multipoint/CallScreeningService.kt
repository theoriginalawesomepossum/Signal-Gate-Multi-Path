package com.signalgate.multipoint

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.signalgate.multipoint.db.AppDatabase
import com.signalgate.multipoint.db.CallLogEntry
import com.signalgate.multipoint.utils.PhoneNumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CallScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "SignalGateScreening"
    }

    override fun onScreenCall(callDetails: Call.Details) {

        val originalPhoneNumber =
            callDetails.handle?.schemeSpecificPart

        if (originalPhoneNumber == null) {

            logAndAllowCall(
                callDetails,
                "No phone number provided"
            )

            return
        }

        val normalizedPhoneNumber =
            PhoneNumberUtils.normalizePhoneNumber(
                originalPhoneNumber
            )

        // FALLBACK NUMBER STORAGE
        val prefs =
            getSharedPreferences(
                "signalgate",
                MODE_PRIVATE
            )

        prefs.edit()
            .putString(
                "LAST_CALL_NUMBER",
                normalizedPhoneNumber
            )
            .apply()

        Log.d(
            TAG,
            "Screening incoming call from: $originalPhoneNumber"
        )

        serviceScope.launch {

            val decision =
                checkBlockingLogic(normalizedPhoneNumber)

            withContext(Dispatchers.Main) {

                when (decision.first) {

                    CallDecision.ALLOW -> {

                        logAndAllowCall(
                            callDetails,
                            decision.second
                        )
                    }

                    CallDecision.BLOCK -> {

                        logAndBlockCall(
                            callDetails,
                            normalizedPhoneNumber,
                            decision.second
                        )
                    }
                }
            }
        }
    }

    private suspend fun checkBlockingLogic(
        normalizedPhoneNumber: String
    ): Pair<CallDecision, String> {

        val db = AppDatabase.getDatabase(applicationContext)
        val unifiedDao = db.unifiedEntryDao()
        val sourceDao = db.sourceDao()

        // 1. Get all matches for this number
        val matches = unifiedDao.findByNumber(normalizedPhoneNumber)
        
        // 2. Get enabled sources to check priorities
        val enabledSources = sourceDao.getEnabledSources().associateBy { it.id }

        // Priority 1: Manual Allow (sourceId = 0, action = ALLOW)
        if (matches.any { it.sourceId == 0 && it.action == "ALLOW" }) {
            return Pair(CallDecision.ALLOW, "Manual Allow-list match")
        }

        // Priority 2: Manual Block (sourceId = 0, action = BLOCK)
        if (matches.any { it.sourceId == 0 && it.action == "BLOCK" }) {
            return Pair(CallDecision.BLOCK, "Manual Block-list match")
        }

        // Priority 3: Pattern Rules (Check all patterns in DB)
        val patterns = unifiedDao.getAllPatterns()
        for (pattern in patterns) {
            if (normalizedPhoneNumber.startsWith(pattern.phoneNumber)) {
                // If it's an ALLOW pattern, allow it immediately
                if (pattern.action == "ALLOW") {
                    return Pair(CallDecision.ALLOW, "Pattern Allow match: ${pattern.phoneNumber}")
                }
                // If it's a BLOCK pattern, we'll block it (unless an allow-list match was found earlier)
                return Pair(CallDecision.BLOCK, "Pattern Block match: ${pattern.phoneNumber}")
            }
        }

        // Priority 4: Aggregated Hub Sources (External lists)
        // Sort matches by source priority
        val hubMatches = matches
            .filter { it.sourceId != 0 && enabledSources.containsKey(it.sourceId) }
            .sortedBy { enabledSources[it.sourceId]?.priority ?: 999 }

        if (hubMatches.isNotEmpty()) {
            val bestMatch = hubMatches.first()
            val sourceName = enabledSources[bestMatch.sourceId]?.name ?: "Unknown Hub"
            
            return if (bestMatch.action == "ALLOW") {
                Pair(CallDecision.ALLOW, "Hub Allow match: $sourceName")
            } else {
                Pair(CallDecision.BLOCK, "Hub Block match: $sourceName")
            }
        }

        return Pair(CallDecision.ALLOW, "No blocking rules matched")
    }

    private fun logAndAllowCall(
        callDetails: Call.Details,
        reason: String
    ) {

        serviceScope.launch {

            val db =
                AppDatabase.getDatabase(applicationContext)

            db.callLogDao().insert(
                CallLogEntry(
                    phoneNumber =
                        callDetails.handle
                            ?.schemeSpecificPart
                            ?: "Unknown",

                    decision =
                        CallDecision.ALLOW.name,

                    reason = reason
                )
            )
        }

        Log.d(
            TAG,
            "Allowing call: $reason"
        )

        val response =
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()

        respondToCall(
            callDetails,
            response
        )
    }

    private fun logAndBlockCall(
        callDetails: Call.Details,
        phoneNumber: String,
        reason: String
    ) {

        serviceScope.launch {

            val db =
                AppDatabase.getDatabase(applicationContext)

            db.callLogDao().insert(
                CallLogEntry(
                    phoneNumber =
                        callDetails.handle
                            ?.schemeSpecificPart
                            ?: "Unknown",

                    decision =
                        CallDecision.BLOCK.name,

                    reason = reason
                )
            )
        }

        Log.d(
            TAG,
            "Blocking call from $phoneNumber"
        )

        val response =
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .build()

        respondToCall(
            callDetails,
            response
        )
    }

    enum class CallDecision {
        ALLOW,
        BLOCK
    }
}
