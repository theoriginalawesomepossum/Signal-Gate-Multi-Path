package com.signalgate.multipoint

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class PhoneStateReceiver : BroadcastReceiver() {

    companion object {
        private var lastState: String? = null
        private var lastNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            lastNumber = incomingNumber
        }

        // Detect call ended
        if (lastState == TelephonyManager.EXTRA_STATE_OFFHOOK &&
            state == TelephonyManager.EXTRA_STATE_IDLE) {
            lastNumber?.let { PostCallNotifier.show(context, it) }
        }

        lastState = state
    }
}
