package com.signalgate.multipoint.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.db.AppDatabase
import com.signalgate.multipoint.db.BlockEntry
import kotlinx.coroutines.launch

class BlockedNumbersViewModel(application: Application) : AndroidViewModel(application) {

    private val blockDao = AppDatabase.getDatabase(application).blockDao()

    private val _blockedNumbers = MutableLiveData<List<BlockEntry>>()
    val blockedNumbers: LiveData<List<BlockEntry>> = _blockedNumbers

    init {
        loadBlockedNumbers()
    }

    fun loadBlockedNumbers() {
        viewModelScope.launch {
            _blockedNumbers.value = blockDao.getAll()
        }
    }

    fun addBlockedNumber(phoneNumber: String, label: String?, isPattern: Boolean) {
        viewModelScope.launch {
            val newEntry = BlockEntry(phoneNumber = phoneNumber, label = label, isPattern = isPattern)
            blockDao.insert(newEntry)
            loadBlockedNumbers() // Refresh list
        }
    }

    fun deleteBlockedNumber(entry: BlockEntry) {
        viewModelScope.launch {
            blockDao.deleteByNumber(entry.phoneNumber)
            loadBlockedNumbers() // Refresh list
        }
    }
}
