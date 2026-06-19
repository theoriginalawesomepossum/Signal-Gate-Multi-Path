package com.signalgate.multipoint.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signalgate.multipoint.data.sources.TrustedSourceCatalog
import com.signalgate.multipoint.database.entities.SourceEntity
import com.signalgate.multipoint.database.repositories.DataSourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SourcesViewModel(
    private val repository: DataSourceRepository
) : ViewModel() {

    val catalog = TrustedSourceCatalog.sources

    private val _selections = MutableStateFlow(
        catalog.associate { it.name to it.defaultEnabled }
    )
    val selections = _selections.asStateFlow()

    fun toggleSource(name: String) {
        _selections.value = _selections.value.toMutableMap().apply {
            put(name, !(this[name] ?: false))
        }
    }

    fun saveSelections() {
        viewModelScope.launch {
            catalog.forEach { source ->
                if (_selections.value[source.name] == true && source.url.isNotBlank()) {
                    repository.insertSource(
                        SourceEntity(
                            name = source.name,
                            type = source.type,
                            pathOrUrl = source.url,
                            isEnabled = true,
                            priority = source.defaultPriority
                        )
                    )
                }
            }
        }
    }
}
