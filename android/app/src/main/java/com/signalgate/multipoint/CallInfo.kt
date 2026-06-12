package com.signalgate.multipoint

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * CallInfo represents information about an incoming call, including spam detection results.
 * This data class is Parcelable to allow passing through Intent extras.
 */
@Parcelize
data class CallInfo(
    val originalPhoneNumber: String,
    val normalizedPhoneNumber: String,
    val spamStatus: String, // e.g., LIKELY SPAM, SPAM, UNKNOWN, SAFE
    val spamCategory: String?, // e.g., Telemarketing, Robocall, Scam
    val confidence: Int?, // 0-100 confidence percentage
    val riskLevel: String?, // e.g., HIGH, MEDIUM, LOW
    val matchedSources: List<String>, // e.g., Community Feed, Telemarketer DB, User Reports
    val callDecision: SignalGateCallScreeningService.CallDecision // The decision for this call
) : Parcelable
