package com.signalgate.multipoint.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.db.AllowEntry
import com.signalgate.multipoint.db.AppDatabase
import com.signalgate.multipoint.db.BlockEntry
import kotlinx.coroutines.launch

class BlockedNumbersViewModel(
    private val appDatabase: AppDatabase
) : ViewModel() {

    private val blockDao = appDatabase.blockDao()
    private val allowDao = appDatabase.allowDao()

    private val _blockedNumbers = MutableLiveData<List<BlockEntry>>()
    val blockedNumbers: LiveData<List<BlockEntry>> = _blockedNumbers

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    init {
        loadBlockedNumbers()
    }

    private fun loadBlockedNumbers() {
        viewModelScope.launch {
            _blockedNumbers.value = blockDao.getAll()
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
            _actionResult.value = "Block rule added: $phoneNumber"
            loadBlockedNumbers()
        }
    }

    fun addPatternRule(pattern: String, label: String?) {
        viewModelScope.launch {
            val newEntry = BlockEntry(
                phoneNumber = pattern,
                label = label,
                isPattern = true
            )
            blockDao.insert(newEntry)
            _actionResult.value = "Pattern rule added: $pattern"
            loadBlockedNumbers()
        }
    }

    fun deleteBlockedNumber(entry: BlockEntry) {
        viewModelScope.launch {
            blockDao.deleteByNumber(entry.phoneNumber)
            _actionResult.value = "Removed from blocklist: ${entry.phoneNumber}"
            loadBlockedNumbers()
        }
    }

    fun addToWhitelist(phoneNumber: String) {
        viewModelScope.launch {
            val newEntry = AllowEntry(
                phoneNumber = phoneNumber
            )
            allowDao.insert(newEntry)
            _actionResult.value = "Added to whitelist: $phoneNumber"
            loadBlockedNumbers()
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
