package com.signalgate.multipoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.signalgate.multipoint.ui.theme.SignalGateTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.signalgate.multipoint.OperationalDashboardCompose
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignalGateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: DashboardViewModel = koinViewModel()) {
    val dataSources by viewModel.dataSources.collectAsState(initial = emptyList())
    val totalSources by viewModel.totalSources.collectAsState(initial = 0)
    val totalEntries by viewModel.totalEntries.collectAsState(initial = 0)
    val blockedToday by viewModel.blockedToday.collectAsState(initial = 0)

    OperationalDashboardCompose(
        dataSources = dataSources,
        totalSources = totalSources,
        totalEntries = totalEntries,
        blockedToday = blockedToday,
        onSyncAll = { viewModel.syncAllSources() },
        onAddSource = { /* TODO */ }
    )
}
