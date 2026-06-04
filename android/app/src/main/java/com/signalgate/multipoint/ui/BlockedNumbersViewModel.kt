package com.signalgate.multipoint.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.db.PhoneEntry
import com.signalgate.multipoint.repository.DataSourceRepository
import kotlinx.coroutines.launch

class BlockedNumbersViewModel(
    private val repository: DataSourceRepository
) : ViewModel() {

    private val _blockedNumbers = MutableLiveData<List<PhoneEntry>>()
    val blockedNumbers: LiveData<List<PhoneEntry>> = _blockedNumbers

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    init {
        loadBlockedNumbers()
    }

    private fun loadBlockedNumbers() {
        viewModelScope.launch {
            _blockedNumbers.value = emptyList() // TODO: Add DAO query later
        }
    }

    fun addBlockedNumber(phoneNumber: String, label: String? = null, isPattern: Boolean = false) {
        viewModelScope.launch {
            val entry = PhoneEntry(
                phoneNumber = phoneNumber,
                action = PhoneEntry.ActionType.BLOCK,
                isPattern = isPattern,
                metadata = label?.let { "Label: $it" } ?: "Manual block"
            )
            repository.insertEntry(entry)
            _actionResult.value = "✅ Blocked: $phoneNumber"
            loadBlockedNumbers()
        }
    }

    fun addToWhitelist(phoneNumber: String) {
        viewModelScope.launch {
            val entry = PhoneEntry(
                phoneNumber = phoneNumber,
                action = PhoneEntry.ActionType.ALLOW,
                metadata = "Manual whitelist"
            )
            repository.insertEntry(entry)
            _actionResult.value = "✅ Whitelisted: $phoneNumber"
            loadBlockedNumbers()
        }
    }

    fun deleteBlockedNumber(phoneNumber: String) {
        viewModelScope.launch {
            // TODO: Implement delete in PhoneEntryDao
            _actionResult.value = "🗑️ Removed: $phoneNumber"
            loadBlockedNumbers()
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
