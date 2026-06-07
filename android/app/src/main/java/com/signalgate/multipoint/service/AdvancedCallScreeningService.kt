package com.signalgate.multipoint.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.signalgate.multipoint.data.dao.CallLogDao
import com.signalgate.multipoint.data.dao.CallLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdvancedCallScreeningService : CallScreeningService() {

    // Dynamically inject the local database instance using your Koin architecture
    private val callLogDao: CallLogDao by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val incomingNumber = callDetails.handle?.schemeSpecificPart ?: return
        
        // 1. Immediately issue an asynchronous evaluation payload to prevent system blocking
        serviceScope.launch {
            val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Dummy placeholder record representing an intercepted threat vector
            val mockEntity = CallLogEntity(
                number = incomingNumber,
                timestamp = System.currentTimeMillis(),
                formattedTime = currentTime,
                dispositionType = "SPAM",
                cachedGeoLocation = "Detected Vector",
                matchedFeedsList = listOf("Community Spam Feed"),
                confidenceScore = 94
            )
            
            // Persist straight to the Room database; your Compose UI updates instantly[cite: 1]
            callLogDao.insertCallRecord(mockEntity)
        }

        // 2. Respond to the Android System with standard blocking instructions
        val response = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
    }
}