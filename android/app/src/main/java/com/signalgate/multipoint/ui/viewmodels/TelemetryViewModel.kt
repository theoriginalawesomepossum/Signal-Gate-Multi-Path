package com.signalgate.multipoint.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.data.models.CallLogItem
import com.signalgate.multipoint.data.models.CallType
import com.signalgate.multipoint.data.repository.CallLogRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TelemetryViewModel(
    private val repository: CallLogRepository
) : ViewModel() {

    // Transforms database entities into UI-consumable data models on the fly
    val liveCallTelemetry: StateFlow<List<CallLogItem>> = repository.allLogsFlow
        .map { entityList ->
            entityList.map { entity ->
                // Map database fields to your custom visual UI models cleanly
                CallLogItem(
                    id = entity.id.toString(),
                    phoneNumber = entity.number,
                    location = entity.cachedGeoLocation ?: "Unknown Location",
                    timestamp = entity.formattedTime,
                    type = CallType.valueOf(entity.dispositionType),
                    matchedSources = entity.matchedFeedsList ?: emptyList(),
                    riskConfidence = entity.confidenceScore
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Drop connection when UI backgrounded
            initialValue = emptyList()
        )
}