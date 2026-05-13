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

    // NEW: Error handling
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadBlockedNumbers()
    }

    private fun loadBlockedNumbers() {
        viewModelScope.launch {
            try {
                _error.value = null
                _blockedNumbers.value = blockDao.getAll()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to load blocked numbers: ${e.message}"
                _blockedNumbers.value = emptyList()
            }
        }
    }

    fun addBlockedNumber(phoneNumber: String, label: String?, isPattern: Boolean) {
        viewModelScope.launch {
            try {
                val newEntry = BlockEntry(
                    phoneNumber = phoneNumber,
                    label = label,
                    isPattern = isPattern
                )
                blockDao.insert(newEntry)
                _actionResult.value = "Block rule added: $phoneNumber"
                loadBlockedNumbers()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to add block rule: ${e.message}"
            }
        }
    }

    fun addPatternRule(pattern: String, label: String?) {
        viewModelScope.launch {
            try {
                val newEntry = BlockEntry(
                    phoneNumber = pattern,
                    label = label,
                    isPattern = true
                )
                blockDao.insert(newEntry)
                _actionResult.value = "Pattern rule added: $pattern"
                loadBlockedNumbers()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to add pattern rule: ${e.message}"
            }
        }
    }

    fun deleteBlockedNumber(entry: BlockEntry) {
        viewModelScope.launch {
            try {
                blockDao.deleteByNumber(entry.phoneNumber)
                _actionResult.value = "Removed from blocklist: ${entry.phoneNumber}"
                loadBlockedNumbers()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to delete number: ${e.message}"
            }
        }
    }

    fun addToWhitelist(phoneNumber: String) {
        viewModelScope.launch {
            try {
                val newEntry = AllowEntry(
                    phoneNumber = phoneNumber
                )
                allowDao.insert(newEntry)
                _actionResult.value = "Added to whitelist: $phoneNumber"
                loadBlockedNumbers()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to add to whitelist: ${e.message}"
            }
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }

    // NEW: Clear error after showing it
    fun clearError() {
        _error.value = null
    }
}
