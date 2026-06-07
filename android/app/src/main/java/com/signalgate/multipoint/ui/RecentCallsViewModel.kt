package com.signalgate.multipoint.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.database.entities.CallLogEntry
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import com.signalgate.multipoint.repository.DataSourceRepository
import kotlinx.coroutines.launch

class RecentCallsViewModel(
    private val repository: DataSourceRepository
) : ViewModel() {

    private val _recentCalls = MutableLiveData<List<CallLogEntry>>()
    val recentCalls: LiveData<List<CallLogEntry>> = _recentCalls

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    init {
        loadRecentCalls()
    }

    fun loadRecentCalls() {
        viewModelScope.launch {
            _recentCalls.value = emptyList() // TODO: Implement call log retrieval from repository
        }
    }

    fun addBlockedNumber(phoneNumber: String, label: String? = null, isPattern: Boolean = false) {
        viewModelScope.launch {
            val entry = UnifiedEntryEntity(
                phoneNumber = phoneNumber,
                action = "BLOCK",
                sourceId = 0,
                isPattern = isPattern,
                metadata = "From recent calls"
            )
            repository.insertEntry(entry)
            _actionResult.value = "✅ Blocked: $phoneNumber"
        }
    }

    fun addToWhitelist(phoneNumber: String) {
        viewModelScope.launch {
            val entry = UnifiedEntryEntity(
                phoneNumber = phoneNumber,
                action = "ALLOW",
                sourceId = 0,
                metadata = "From recent calls"
            )
            repository.insertEntry(entry)
            _actionResult.value = "✅ Whitelisted: $phoneNumber"
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
