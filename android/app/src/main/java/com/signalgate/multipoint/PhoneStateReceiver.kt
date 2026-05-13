package com.signalgate.multipoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

class PhoneStateReceiver : BroadcastReceiver() {

    companion object {

        private const val TAG =
            "PhoneStateReceiver"

        private var lastState: String? = null
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val state =
            intent.getStringExtra(
                TelephonyManager.EXTRA_STATE
            )

        Log.d(
            TAG,
            "Phone state changed: $state"
        )

        // Detect ANY call ending
        if (
            lastState != TelephonyManager.EXTRA_STATE_IDLE &&
            state == TelephonyManager.EXTRA_STATE_IDLE
        ) {

            val prefs =
                context.getSharedPreferences(
                    "signalgate",
                    Context.MODE_PRIVATE
                )

            val number =
                prefs.getString(
                    "LAST_CALL_NUMBER",
                    null
                )

            Log.d(
                TAG,
                "Call ended. Number: $number"
            )

            number?.let {

                PostCallNotifier.show(
                    context,
                    it
                )
            }
        }

        lastState = state
    }
}
