package com.signalgate.multipoint.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.db.AllowEntry
import com.signalgate.multipoint.db.AppDatabase
import com.signalgate.multipoint.db.BlockEntry
import com.signalgate.multipoint.db.CallLogEntry
import kotlinx.coroutines.launch

class RecentCallsViewModel(
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val callLogDao = appDatabase.callLogDao()
    private val blockDao = appDatabase.blockDao()
    private val allowDao = appDatabase.allowDao()

    private val _recentCalls = MutableLiveData<List<CallLogEntry>>()
    val recentCalls: LiveData<List<CallLogEntry>> = _recentCalls

    init {
        loadRecentCalls()
    }

    fun loadRecentCalls() {
        viewModelScope.launch {
            _recentCalls.value = callLogDao.getAll()
        }
    }

    fun addBlockedNumber(phoneNumber: String, label: String?, isPattern: Boolean) {
        viewModelScope.launch {
            val newEntry = BlockEntry(
                phoneNumber = phoneNumber,
                label = label,
                isPattern = isPattern
            )
            blockDao.insert(newEntry)
        }
    }

    fun addToWhitelist(phoneNumber: String) {
        viewModelScope.launch {
            val newEntry = AllowEntry(
                phoneNumber = phoneNumber
            )
            allowDao.insert(newEntry)
        }
    }
}
