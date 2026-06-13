package com.signalgate.multipoint.ui.screens

import android.os.Bundle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LogcatViewModel : ViewModel() {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs = _logs.asStateFlow()

    fun captureLogcat() {
        try {
            val process = Runtime.getRuntime().exec("logcat -d -v time SignalGate:*")
            val reader = process.inputStream.bufferedReader()
            _logs.value = reader.readLines().takeLast(500) // last 500 lines
        } catch (e: Exception) {
            _logs.value = listOf("Error capturing logs: ${e.message}")
        }
    }
}

@Composable
fun LogcatViewerScreen(viewModel: LogcatViewModel = LogcatViewModel()) {
    val logs by viewModel.logs.collectAsState()
    
    LaunchedEffect(Unit) { viewModel.captureLogcat() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { viewModel.captureLogcat() }) {
            Text("Refresh Logs")
        }
        
        LazyColumn {
            items(logs) { log ->
                Text(text = log, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
