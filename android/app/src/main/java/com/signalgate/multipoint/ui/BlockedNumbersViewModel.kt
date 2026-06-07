package com.signalgate.multipoint.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import com.signalgate.multipoint.repository.DataSourceRepository
import kotlinx.coroutines.launch

class BlockedNumbersViewModel(
    private val repository: DataSourceRepository
) : ViewModel() {

    private val _blockedNumbers = MutableLiveData<List<UnifiedEntryEntity>>()
    val blockedNumbers: LiveData<List<UnifiedEntryEntity>> = _blockedNumbers

    private val _actionResult = MutableLiveData<String?>()
    val actionResult: LiveData<String?> = _actionResult

    init {
        loadBlockedNumbers()
    }

    private fun loadBlockedNumbers() {
        viewModelScope.launch {
            _blockedNumbers.value = repository.getAllEntries().filter { it.action == "BLOCK" }
        }
    }

    fun addBlockedNumber(phoneNumber: String, label: String? = null, isPattern: Boolean = false) {
        viewModelScope.launch {
            val entry = UnifiedEntryEntity(
                phoneNumber = phoneNumber,
                action = "BLOCK",
                sourceId = 0, // Manual entries use sourceId 0 or a dedicated manual source
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
            val entry = UnifiedEntryEntity(
                phoneNumber = phoneNumber,
                action = "ALLOW",
                sourceId = 0,
                metadata = "Manual whitelist"
            )
            repository.insertEntry(entry)
            _actionResult.value = "✅ Whitelisted: $phoneNumber"
            loadBlockedNumbers()
        }
    }

    fun deleteBlockedNumber(entry: UnifiedEntryEntity) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            _actionResult.value = "🗑️ Removed: ${entry.phoneNumber}"
            loadBlockedNumbers()
        }
    }

    fun clearActionResult() {
        _actionResult.value = null
    }
}
