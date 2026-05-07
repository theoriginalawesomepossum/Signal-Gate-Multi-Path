package com.signalgate.multipoint

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.core.app.NotificationCompat
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
        private const val CHANNEL_ID = "call_post_action_channel"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onScreenCall(callDetails: Call.Details) {
        val originalPhoneNumber = callDetails.handle?.schemeSpecificPart

        if (originalPhoneNumber == null) {
            logAndAllowCall(callDetails, "No phone number provided")
            return
        }

        val normalizedPhoneNumber = PhoneNumberUtils.normalizePhoneNumber(originalPhoneNumber)
        Log.d(TAG, "Screening incoming call from: $originalPhoneNumber (Normalized: $normalizedPhoneNumber)")

        serviceScope.launch {
            val decision = checkBlockingLogic(normalizedPhoneNumber)

            withContext(Dispatchers.Main) {
                when (decision.first) {
                    CallDecision.ALLOW -> logAndAllowCall(callDetails, decision.second)
                    CallDecision.BLOCK -> logAndBlockCall(callDetails, normalizedPhoneNumber, decision.second)
                }
                // Show post-call notification with action buttons
                showPostCallNotification(originalPhoneNumber, normalizedPhoneNumber)
            }
        }
    }

    private suspend fun checkBlockingLogic(normalizedPhoneNumber: String): Pair<CallDecision, String> {
        val db = AppDatabase.getDatabase(applicationContext)
        val blockDao = db.blockDao()
        val allowDao = db.allowDao()

        val allowEntry = allowDao.findByNumber(normalizedPhoneNumber)
        if (allowEntry != null) {
            Log.d(TAG, "Allow match found in allowlist: $normalizedPhoneNumber")
            return Pair(CallDecision.ALLOW, "Exact match in allowlist")
        }

        val exactBlockMatch = blockDao.findByNumber(normalizedPhoneNumber)
        if (exactBlockMatch != null) {
            Log.d(TAG, "Exact match found in blocklist: $normalizedPhoneNumber")
            return Pair(CallDecision.BLOCK, "Exact match in blocklist")
        }

        val allBlockEntries = blockDao.getAll()
        for (entry in allBlockEntries) {
            if (entry.isPattern && normalizedPhoneNumber.startsWith(entry.phoneNumber)) {
                Log.d(TAG, "Pattern match found: $normalizedPhoneNumber matches ${entry.phoneNumber}")
                return Pair(CallDecision.BLOCK, "Pattern match: ${entry.phoneNumber}")
            }
        }

        return Pair(CallDecision.ALLOW, "No blocking rules matched")
    }

    private fun logAndAllowCall(callDetails: Call.Details, reason: String) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.callLogDao().insert(CallLogEntry(
                phoneNumber = callDetails.handle?.schemeSpecificPart ?: "Unknown",
                decision = CallDecision.ALLOW.name,
                reason = reason
            ))
        }
        Log.d(TAG, "Allowing call. Reason: $reason")
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        respondToCall(callDetails, response)
    }

    private fun logAndBlockCall(callDetails: Call.Details, phoneNumber: String, reason: String) {
        serviceScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            db.callLogDao().insert(CallLogEntry(
                phoneNumber = callDetails.handle?.schemeSpecificPart ?: "Unknown",
                decision = CallDecision.BLOCK.name,
                reason = reason
            ))
        }
        Log.d(TAG, "Blocking call from: $phoneNumber. Reason: $reason")
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(true)
            .build()
        respondToCall(callDetails, response)
    }

    private fun showPostCallNotification(originalPhoneNumber: String, normalizedPhoneNumber: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Post-Call Actions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Actions for recently ended calls"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Call from $originalPhoneNumber ended")
            .setContentText("What would you like to do?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Action 1: Block permanently
        val blockIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACTION_BLOCK_PERMANENT"
            putExtra("PHONE_NUMBER", normalizedPhoneNumber)
        }
        val blockPendingIntent = PendingIntent.getBroadcast(this, 0, blockIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Action 2: Whitelist
        val whitelistIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACTION_WHITELIST"
            putExtra("PHONE_NUMBER", normalizedPhoneNumber)
        }
        val whitelistPendingIntent = PendingIntent.getBroadcast(this, 1, whitelistIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Action 3: Block prefix
        val prefixIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACTION_BLOCK_PREFIX"
            putExtra("PHONE_NUMBER", normalizedPhoneNumber)
        }
        val prefixPendingIntent = PendingIntent.getBroadcast(this, 2, prefixIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Action 4: Block area code
        val areaCodeIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACTION_BLOCK_AREA_CODE"
            putExtra("PHONE_NUMBER", normalizedPhoneNumber)
        }
        val areaCodePendingIntent = PendingIntent.getBroadcast(this, 3, areaCodeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Action 5: Ignore
        val ignoreIntent = Intent(this, CallActionReceiver::class.java).apply {
            action = "ACTION_IGNORE"
            putExtra("PHONE_NUMBER", normalizedPhoneNumber)
        }
        val ignorePendingIntent = PendingIntent.getBroadcast(this, 4, ignoreIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        notificationBuilder.addAction(NotificationCompat.Action(0, "Block Permanently", blockPendingIntent))
        notificationBuilder.addAction(NotificationCompat.Action(0, "Whitelist", whitelistPendingIntent))
        notificationBuilder.addAction(NotificationCompat.Action(0, "Block Prefix", prefixPendingIntent))
        notificationBuilder.addAction(NotificationCompat.Action(0, "Block Area Code", areaCodePendingIntent))
        notificationBuilder.addAction(NotificationCompat.Action(0, "Ignore", ignorePendingIntent))

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    enum class CallDecision { ALLOW, BLOCK }
}
