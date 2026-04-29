package com.signalgate.multipoint

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.signalgate.multipoint.db.AppDatabase
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
        // Handle is the URI for the call (e.g., tel:+1234567890)
        val phoneNumber = callDetails.handle?.schemeSpecificPart

        if (phoneNumber == null) {
            allowCall(callDetails)
            return
        }

        Log.d(TAG, "Screening incoming call from: $phoneNumber")

        // Run blocking check in a coroutine
        serviceScope.launch {
            val shouldBlock = checkBlockingLogic(phoneNumber)
            
            withContext(Dispatchers.Main) {
                if (shouldBlock) {
                    blockCall(callDetails, phoneNumber)
                } else {
                    allowCall(callDetails)
                }
            }
        }
    }

    private suspend fun checkBlockingLogic(phoneNumber: String): Boolean {
        val db = AppDatabase.getDatabase(applicationContext)
        val blockDao = db.blockDao()

        // 1. Check exact match in local database
        val exactMatch = blockDao.findByNumber(phoneNumber)
        if (exactMatch != null) {
            Log.d(TAG, "Exact match found in blocklist: $phoneNumber")
            return true
        }

        // 2. Smart Logic: Check for patterns (simple prefix check for now)
        // In a real app, we'd fetch all pattern entries and check regex or prefixes
        val allEntries = blockDao.getAll()
        for (entry in allEntries) {
            if (entry.isPattern && phoneNumber.startsWith(entry.phoneNumber)) {
                Log.d(TAG, "Pattern match found: $phoneNumber matches ${entry.phoneNumber}")
                return true
            }
        }

        // 3. Potential for further intelligence (e.g., STIR/SHAKEN, frequency, etc.)
        // For now, if not in blocklist, we allow it.
        return false
    }

    private fun allowCall(callDetails: Call.Details) {
        Log.d(TAG, "Allowing call")
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)
    }

    private fun blockCall(callDetails: Call.Details, phoneNumber: String) {
        Log.d(TAG, "Blocking call from: $phoneNumber")
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(true)
            .build()
        respondToCall(callDetails, response)
    }
}
