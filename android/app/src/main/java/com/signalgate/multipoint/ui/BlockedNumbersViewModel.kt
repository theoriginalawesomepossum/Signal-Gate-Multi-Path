package com.signalgate.multipoint.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import com.signalgate.multipoint.repository.DataSourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BlockedNumbersViewModel(private val repository: DataSourceRepository) : ViewModel() {

    private val _blockedNumbers = MutableStateFlow<List<UnifiedEntryEntity>>(emptyList())
    val blockedNumbers: StateFlow<List<UnifiedEntryEntity>> = _blockedNumbers

    init {
        loadBlockedNumbers()
    }

    fun loadBlockedNumbers() {
        viewModelScope.launch {
            _blockedNumbers.value = repository.getAllEntries().filter { it.action == "BLOCK" }
        }
    }

    // Fixed: Accepted type updated from PhoneEntry to UnifiedEntryEntity
    fun unblockNumber(entry: UnifiedEntryEntity) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            loadBlockedNumbers()
        }
    }

    // Fixed: Accepted type updated from PhoneEntry to UnifiedEntryEntity
    fun blockNumber(entry: UnifiedEntryEntity) {
        viewModelScope.launch {
            repository.insertEntry(entry)
            loadBlockedNumbers()
        }
    }
}
