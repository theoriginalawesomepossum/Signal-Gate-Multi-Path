package com.signalgate.multipoint.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R
import com.signalgate.multipoint.adapters.DataSourceAdapter
import com.signalgate.multipoint.models.DataSource
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
import kotlinx.coroutines.launch

/**
 * DashboardFragment displays the main operational overview of the app.
 * Integrates with DashboardViewModel for state management and data persistence.
 */
class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var dataSourceAdapter: DataSourceAdapter
    private val dataSources = mutableListOf<DataSource>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up Data Sources RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.data_sources_recycler_view)
        dataSourceAdapter = DataSourceAdapter(
            dataSources,
            viewModel,
            onSourceToggled = { _, _ ->
                // LED will update automatically via the ViewModel
            },
            onSyncClicked = { _ ->
                // Sync is handled by ViewModel
            },
            onSettingsClicked = { _ ->
                // TODO: Show settings dialog for this source
            }
        )
        recyclerView.adapter = dataSourceAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Load sample data
        loadSampleDataSources()
        
        // Update top stats
        updateStats(view)
        
        // Observe ViewModel flows for real-time updates
        observeViewModel(view)
    }

    /**
     * Observes ViewModel flows for real-time updates to the UI.
     */
    private fun observeViewModel(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Observe total sources count
            viewModel.totalSources.collect { count ->
                @Suppress("UNUSED_VARIABLE") val totalSourcesText = view.findViewById<TextView>(R.id.total_sources_text)
                totalSourcesText?.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe total entries count
            viewModel.totalEntries.collect { count ->
                val totalEntriesText = view.findViewById<TextView>(R.id.total_entries_text)
                totalEntriesText?.text = String.format("%,d", count)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe enabled sources count
            viewModel.enabledSourcesCount.collect { _ ->
                @Suppress("UNUSED_VARIABLE") val totalSourcesText = view.findViewById<TextView>(R.id.total_sources_text)
                @Suppress("UNUSED_VARIABLE") val subtitle = view.findViewById<TextView>(R.id.total_sources_text)?.parent?.let {
                    (it as? android.view.ViewGroup)?.findViewWithTag<TextView>("enabled_count")
                }
                // Update enabled count in the stats section
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe blocked today
            viewModel.blockedToday.collect { count ->
                val blockedTodayText = view.findViewById<TextView>(R.id.blocked_today_text)
                blockedTodayText?.text = count.toString()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            // Observe LED states
            viewModel.ledStates.collect { _ ->
                // LED states are automatically handled by the adapter
            }
        }
    }

    /**
     * Loads the specific 8 data sources requested by the user.
     */
    private fun loadSampleDataSources() {
        dataSources.clear()
        
        // 1. App Whitelist
        dataSources.add(DataSource(1, "App Whitelist", "Local CSV", 1204, "Healthy", "1m ago", true))
        
        // 2. App Blocklist
        dataSources.add(DataSource(2, "App Blocklist", "Local CSV", 24610, "Healthy", "5m ago", true))
        
        // 3. GitHub Community Blocklist
        dataSources.add(DataSource(3, "GitHub Community Blocklist", "Remote URL", 0, "Healthy", "Never", false))
        
        // 4. Local File 1
        dataSources.add(DataSource(4, "Local File 1", "Local CSV", 450, "Healthy", "10m ago", true))
        
        // 5. Local File 2
        dataSources.add(DataSource(5, "Local File 2", "Local CSV", 890, "Healthy", "12m ago", true))
        
        // 6. Archived Spamlist
        dataSources.add(DataSource(6, "Archived Spamlist", "Local CSV", 856, "Disabled", "Never", false))
        
        // 7. User Remote URL 1
        dataSources.add(DataSource(7, "User Remote URL 1", "Remote URL", 127854, "Healthy", "1m ago", true))
        
        // 8. User Remote URL 2
        dataSources.add(DataSource(8, "User Remote URL 2", "Remote URL", 46122, "Error", "23m ago", true))
        
        dataSourceAdapter.notifyDataSetChanged()
    }

    /**
     * Updates the stats displayed at the top of the dashboard.
     */
    private fun updateStats(view: View) {
        val totalSourcesText = view.findViewById<TextView>(R.id.total_sources_text)
        val totalEntriesText = view.findViewById<TextView>(R.id.total_entries_text)
        val activeShieldText = view.findViewById<TextView>(R.id.active_shield_text)
        
        val totalEntries = dataSources.sumOf { it.entriesCount }
        val activeSources = dataSources.count { it.isEnabled }
        
        totalSourcesText?.text = dataSources.size.toString()
        totalEntriesText?.text = String.format("%,d", totalEntries)
        activeShieldText?.text = if (activeSources > 0) "ACTIVE" else "INACTIVE"
    }
}
