package com.signalgate.multipoint.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.database.SignalGateDatabase
import com.signalgate.multipoint.database.entities.SourceEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for the Operational Dashboard.
 * Manages data flow between the SignalGateDatabase and the UI.
 * Handles state management for data sources with toggle and LED logic.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SignalGateDatabase.getInstance(application)
    private val sourceDao = database.sourceDao()
    private val entryDao = database.unifiedEntryDao()
    private val callLogDao = database.callLogDao()

    // Dashboard Stats
    val totalSources: Flow<Int> = sourceDao.getSourceCount()
    val totalEntries: Flow<Int> = entryDao.getTotalEntryCount()
    
    private val _blockedToday = MutableStateFlow(0)
    val blockedToday: StateFlow<Int> = _blockedToday.asStateFlow()

    // Data Sources - flows directly from database
    val dataSources: Flow<List<SourceEntity>> = sourceDao.getAllSources()
    
    // Enabled sources count for stats
    val enabledSourcesCount: Flow<Int> = sourceDao.getAllSources()
        .map { sources -> sources.count { it.isEnabled } }

    // Sync Status
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    // LED state tracking - maps source ID to whether its LED should be on
    private val _ledStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val ledStates: StateFlow<Map<Int, Boolean>> = _ledStates.asStateFlow()

    init {
        refreshBlockedToday()
        observeDataSources()
    }

    /**
     * Observes data sources and updates LED states based on enabled status.
     */
    private fun observeDataSources() {
        viewModelScope.launch {
            dataSources.collect { sources ->
                val newLedStates = mutableMapOf<Int, Boolean>()
                sources.forEach { source ->
                    newLedStates[source.id] = source.isEnabled
                }
                _ledStates.value = newLedStates
            }
        }
    }

    /**
     * Refreshes the count of calls blocked today.
     */
    fun refreshBlockedToday() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = calendar.timeInMillis
            _blockedToday.value = callLogDao.getBlockedCallsCount(startTime)
        }
    }

    /**
     * Toggles the enabled state of a data source.
     * This will automatically update the LED indicator via the ledStates flow.
     */
    fun toggleSourceEnabled(sourceId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            sourceDao.updateSourceEnabled(sourceId, isEnabled)
            // LED state will be updated automatically via the dataSources flow
        }
    }

    /**
     * Triggers a sync for a specific data source.
     */
    fun syncSource(sourceId: Int) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                // TODO: Implement actual sync logic via DataSyncEngine
                // For now, we'll simulate a sync update
                sourceDao.updateSourceSyncStatus(
                    id = sourceId,
                    timestamp = System.currentTimeMillis(),
                    entriesCount = entryDao.getEntryCountBySourceId(sourceId),
                    healthStatus = "HEALTHY"
                )
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Triggers a sync for all enabled data sources.
     */
    fun syncAllSources() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                // TODO: Implement actual sync all logic via DataSyncEngine
                dataSources.first().forEach { source ->
                    if (source.isEnabled) {
                        sourceDao.updateSourceSyncStatus(
                            id = source.id,
                            timestamp = System.currentTimeMillis(),
                            entriesCount = entryDao.getEntryCountBySourceId(source.id),
                            healthStatus = "HEALTHY"
                        )
                    }
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    /**
     * Gets the LED state for a specific source.
     */
    fun getLedState(sourceId: Int): Boolean {
        return _ledStates.value[sourceId] ?: false
    }
}
