package com.signalgate.multipoint.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.database.entities.SourceEntity
import com.signalgate.multipoint.database.repositories.DataSourceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(
    private val repository: DataSourceRepository
) : ViewModel() {

    val totalSources: Flow<Int> = repository.getSourceCount()
    val totalEntries: Flow<Int> = repository.getTotalEntryCount()

    private val _blockedToday = MutableStateFlow(0)
    val blockedToday: StateFlow<Int> = _blockedToday.asStateFlow()

    val dataSources: Flow<List<SourceEntity>> = repository.getAllSources()
    val enabledSourcesCount: Flow<Int> = repository.getEnabledSourceCount()
    val enabledSourcesEntryCount: Flow<Int> = repository.getEnabledSourcesEntryCount()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _ledStates = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val ledStates: StateFlow<Map<Int, Boolean>> = _ledStates.asStateFlow()

    init {
        observeDataSources()
    }

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

    fun toggleSourceEnabled(sourceId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleSourceEnabled(sourceId, isEnabled)
        }
    }

    fun syncSource(sourceId: Int) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val entriesCount = repository.getEntryCountBySourceId(sourceId)
                repository.updateSourceSyncStatus(
                    sourceId = sourceId,
                    timestamp = System.currentTimeMillis(),
                    entriesCount = entriesCount,
                    healthStatus = "HEALTHY"
                )
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun syncAllSources() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                dataSources.first().forEach { source ->
                    if (source.isEnabled) {
                        val entriesCount = repository.getEntryCountBySourceId(source.id)
                        repository.updateSourceSyncStatus(
                            sourceId = source.id,
                            timestamp = System.currentTimeMillis(),
                            entriesCount = entriesCount,
                            healthStatus = "HEALTHY"
                        )
                    }
                }
            } finally {
                _isSyncing.value = false
            }
        }
    }
}

class DashboardViewModelFactory(
    private val repository: DataSourceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
