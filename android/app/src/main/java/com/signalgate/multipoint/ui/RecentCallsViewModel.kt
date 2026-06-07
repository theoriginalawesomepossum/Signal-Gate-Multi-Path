package com.signalgate.multipoint.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import com.signalgate.multipoint.repository.DataSourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecentCallsViewModel(private val repository: DataSourceRepository) : ViewModel() {

    private val _recentCalls = MutableStateFlow<List<UnifiedEntryEntity>>(emptyList())
    val recentCalls: StateFlow<List<UnifiedEntryEntity>> = _recentCalls

    init {
        loadRecentCalls()
    }

    fun loadRecentCalls() {
        viewModelScope.launch {
            _recentCalls.value = repository.getAllEntries()
        }
    }

    // Fixed: Accepted type updated from PhoneEntry to UnifiedEntryEntity
    fun removeLog(entry: UnifiedEntryEntity) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            loadRecentCalls()
        }
    }

    // Fixed: Accepted type updated from PhoneEntry to UnifiedEntryEntity
    fun addLog(entry: UnifiedEntryEntity) {
        viewModelScope.launch {
            repository.insertEntry(entry)
            loadRecentCalls()
        }
    }
}
