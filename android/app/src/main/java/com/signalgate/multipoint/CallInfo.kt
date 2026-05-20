package com.signalgate.multipoint

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CallInfo(
    val originalPhoneNumber: String,
    val normalizedPhoneNumber: String,
    val spamStatus: String,
    val spamCategory: String?,
    val confidence: Int?,
    val riskLevel: String?,
    val matchedSources: List<String>,
    val callDecision: CallDecision
) : Parcelable
