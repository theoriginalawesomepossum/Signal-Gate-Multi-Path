package com.signalgate.multipoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.signalgate.multipoint.overlay.OverlayManagerService

/**
 * PhoneStateReceiver — PATCHED for Phase 3
 *
 * Change summary vs. original:
 *   - RINGING state → fires ACTION_SHOW to OverlayManagerService
 *   - IDLE state    → fires ACTION_HIDE  to OverlayManagerService
 *   - PostCallNotifier.show() call is preserved (notification still fires on IDLE)
 *
 * The receiver does NOT make any screening/DB decisions itself.
 * It only signals the overlay engine about call lifecycle transitions.
 */
class PhoneStateReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PhoneStateReceiver"
        private var lastState: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d(TAG, "Phone state changed: $state")

        when (state) {

            TelephonyManager.EXTRA_STATE_RINGING -> {
                // A new call is arriving — show the shield in "SCREENING" state.
                // CallScreeningService will call ACTION_UPDATE once it has a decision.
                if (!OverlayManagerService.checkOverlayPermission(context)) {
                    Log.w(TAG, "Overlay permission not granted — skipping shield")
                } else {
                    val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        ?: context.getSharedPreferences("signalgate", Context.MODE_PRIVATE)
                            .getString("LAST_CALL_NUMBER", "Unknown")
                        ?: "Unknown"

                    val showIntent = Intent(context, OverlayManagerService::class.java).apply {
                        action = OverlayManagerService.ACTION_SHOW
                        putExtra(OverlayManagerService.EXTRA_PHONE_NUMBER, number)
                        putExtra(OverlayManagerService.EXTRA_DECISION, "SCREENING")
                        putExtra(OverlayManagerService.EXTRA_REASON, "Evaluating…")
                    }
                    context.startService(showIntent)
                }
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended — hide the shield
                val hideIntent = Intent(context, OverlayManagerService::class.java).apply {
                    action = OverlayManagerService.ACTION_HIDE
                }
                context.startService(hideIntent)

                // Preserve original post-call notification behavior
                if (lastState != TelephonyManager.EXTRA_STATE_IDLE) {
                    val prefs = context.getSharedPreferences("signalgate", Context.MODE_PRIVATE)
                    val number = prefs.getString("LAST_CALL_NUMBER", null)
                    Log.d(TAG, "Call ended. Number: $number")
                    number?.let { PostCallNotifier.show(context, it) }
                }
            }

            // OFFHOOK = call answered — no overlay change needed; shield stays visible
            // and will have already been updated by CallScreeningService via ACTION_UPDATE
        }

        lastState = state
    }
}
