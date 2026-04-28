package com.signalgate.multipoint

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class CallScreeningService : CallScreeningService() {

    companion object {
        private const val TAG = "SignalGateCallScreening"
    }

    override fun onScreenCall(callDetails: Call.Details) {

        val phoneNumber = callDetails.handle?.schemeSpecificPart

        Log.d(TAG, "Incoming call from: $phoneNumber")

        if (phoneNumber == null) {
            respondToCall(
                callDetails,
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .build()
            )
            return
        }

        // SIMPLE NATIVE DECISION LOGIC (NO REACT NATIVE)
        val shouldBlock = false // <-- replace with your filtering logic later

        val response = if (shouldBlock) {
            Log.d(TAG, "Blocking call: $phoneNumber")

            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .build()

        } else {
            Log.d(TAG, "Allowing call: $phoneNumber")

            CallResponse.Builder()
                .setDisallowCall(false)
                .build()
        }

        respondToCall(callDetails, response)
    }
}
