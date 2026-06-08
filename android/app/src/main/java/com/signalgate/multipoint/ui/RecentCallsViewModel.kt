package com.signalgate.multipoint.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.database.daos.CallLogDao
import com.signalgate.multipoint.database.daos.UnifiedEntryDao
import com.signalgate.multipoint.database.entities.CallLogEntry
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecentCallsViewModel(
    private val callLogDao: CallLogDao,
    private val unifiedEntryDao: UnifiedEntryDao
) : ViewModel() {

    private val _recentCalls = MutableStateFlow<List<CallLogEntry>>(emptyList())
    val recentCalls: StateFlow<List<CallLogEntry>> = _recentCalls

    init {
        loadRecentCalls()
    }

    fun loadRecentCalls() {
        viewModelScope.launch {
            callLogDao.getRecentCalls(100).collect {
                _recentCalls.value = it
            }
        }
    }

    fun blockNumber(phoneNumber: String) {
        viewModelScope.launch {
            unifiedEntryDao.insertEntry(
                UnifiedEntryEntity(
                    phoneNumber = phoneNumber,
                    action = "BLOCK",
                    sourceId = 1 // Assuming 1 is the MANUAL source ID
                )
            )
        }
    }

    fun whitelistNumber(phoneNumber: String) {
        viewModelScope.launch {
            unifiedEntryDao.insertEntry(
                UnifiedEntryEntity(
                    phoneNumber = phoneNumber,
                    action = "ALLOW",
                    sourceId = 1 // Assuming 1 is the MANUAL source ID
                )
            )
        }
    }
}
