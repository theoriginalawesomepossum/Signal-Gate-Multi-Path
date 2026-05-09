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
            logAndAllowCall(callDetails, "No phone number provided")
            return
        }

        val normalizedPhoneNumber =
            PhoneNumberUtils.normalizePhoneNumber(originalPhoneNumber)

        // Save last number for PhoneStateReceiver fallback
        val prefs = getSharedPreferences("signalgate", MODE_PRIVATE)

        prefs.edit()
            .putString("LAST_CALL_NUMBER", normalizedPhoneNumber)
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

                    CallDecision.ALLOW ->
                        logAndAllowCall(
                            callDetails,
                            decision.second
                        )

                    CallDecision.BLOCK ->
                        logAndBlockCall(
                            callDetails,
                            normalizedPhoneNumber,
                            decision.second
                        )
                }
            }
        }
    }

    private suspend fun checkBlockingLogic(
        normalizedPhoneNumber: String
    ): Pair<CallDecision, String> {

        val db =
            AppDatabase.getDatabase(applicationContext)

        val blockDao = db.blockDao()
        val allowDao = db.allowDao()

        val allowEntry =
            allowDao.findByNumber(normalizedPhoneNumber)

        if (allowEntry != null) {

            return Pair(
                CallDecision.ALLOW,
                "Exact match in allowlist"
            )
        }

        val exactBlockMatch =
            blockDao.findByNumber(normalizedPhoneNumber)

        if (exactBlockMatch != null) {

            return Pair(
                CallDecision.BLOCK,
                "Exact match in blocklist"
            )
        }

        val allBlockEntries = blockDao.getAll()

        for (entry in allBlockEntries) {

            if (
                entry.isPattern &&
                normalizedPhoneNumber.startsWith(entry.phoneNumber)
            ) {

                return Pair(
                    CallDecision.BLOCK,
                    "Pattern match: ${entry.phoneNumber}"
                )
            }
        }

        return Pair(
            CallDecision.ALLOW,
            "No blocking rules matched"
        )
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
                        callDetails.handle?.schemeSpecificPart
                            ?: "Unknown",

                    decision =
                        CallDecision.ALLOW.name,

                    reason = reason
                )
            )
        }

        Log.d(TAG, "Allowing call: $reason")

        val response =
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()

        respondToCall(callDetails, response)
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
                        callDetails.handle?.schemeSpecificPart
                            ?: "Unknown",

                    decision =
                        CallDecision.BLOCK.name,

                    reason = reason
                )
            )
        }

        Log.d(TAG, "Blocking call from $phoneNumber")

        val response =
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .build()

        respondToCall(callDetails, response)
    }

    enum class CallDecision {
        ALLOW,
        BLOCK
    }
}
