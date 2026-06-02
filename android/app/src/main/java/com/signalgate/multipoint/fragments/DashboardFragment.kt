package com.signalgate.multipoint.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.signalgate.multipoint.OperationalDashboardCompose
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
import com.signalgate.multipoint.ui.theme.SignalGateTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                SignalGateTheme {
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
            }
        }
    }
}
