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
 * Manages data flow between the SignalGateDatabase and the Compose UI.
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

    // Data Sources
    val dataSources: Flow<List<SourceEntity>> = sourceDao.getAllSources()

    // Sync Status
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    init {
        refreshBlockedToday()
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
     */
    fun toggleSourceEnabled(sourceId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            sourceDao.updateSourceEnabled(sourceId, isEnabled)
        }
    }

    /**
     * Triggers a sync for a specific data source.
     */
    fun syncSource(sourceId: Int) {
        viewModelScope.launch {
            _isSyncing.value = true
            // TODO: Implement actual sync logic via DataSyncEngine
            // For now, we'll simulate a sync update
            sourceDao.updateSourceSyncStatus(
                id = sourceId,
                timestamp = System.currentTimeMillis(),
                entriesCount = entryDao.getEntryCountBySourceId(sourceId),
                healthStatus = "HEALTHY"
            )
            _isSyncing.value = false
        }
    }

    /**
     * Triggers a sync for all enabled data sources.
     */
    fun syncAllSources() {
        viewModelScope.launch {
            _isSyncing.value = true
            // TODO: Implement actual sync all logic via DataSyncEngine
            _isSyncing.value = false
        }
    }
}
