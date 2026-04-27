package com.signalgate.multipoint

import android.content.Context
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

/**
 * CallScreeningService - Native Android service for intercepting calls
 * 
 * This service is registered with Android's telecom system to intercept
 * incoming calls and apply the call screening logic from the React Native app.
 */
class CallScreeningService : CallScreeningService() {
    companion object {
        private const val TAG = "SignalGateCallScreening"
    }

    override fun onScreenCall(call: Call.Details) {
        Log.d(TAG, "Screening call from: ${call.handle}")

        // Get the phone number from the call
        val phoneNumber = call.handle?.schemeSpecificPart ?: return

        // Notify the React Native module about the incoming call
        try {
            CallScreeningModule.instance?.onIncomingCall(
                phoneNumber = phoneNumber,
                displayName = call.callerDisplayName,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error notifying React Native module:", e)
            // Allow the call if there's an error
            respondToCall(call, CallResponse.Builder().setDisallowCall(false).build())
        }
    }

    /**
     * Respond to a call with a decision
     */
    fun respondToCall(call: Call.Details, decision: String) {
        try {
            val response = when (decision) {
                "BLOCK" -> {
                    Log.d(TAG, "Blocking call from: ${call.handle}")
                    CallResponse.Builder()
                        .setDisallowCall(true)
                        .setRejectCall(true)
                        .build()
                }
                else -> {
                    Log.d(TAG, "Allowing call from: ${call.handle}")
                    CallResponse.Builder()
                        .setDisallowCall(false)
                        .build()
                }
            }
            respondToCall(call, response)
        } catch (e: Exception) {
            Log.e(TAG, "Error responding to call:", e)
        }
    }
}
