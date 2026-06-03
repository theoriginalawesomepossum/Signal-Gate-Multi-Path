package com.signalgate.multipoint.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R
import com.signalgate.multipoint.adapters.DataSourceAdapter
import com.signalgate.multipoint.models.DataSource

/**
 * DashboardFragment displays the main dashboard with stats and data sources.
 */
class DashboardFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dataSourceAdapter: DataSourceAdapter
    private var dataSources = mutableListOf<DataSource>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        recyclerView = view.findViewById(R.id.data_sources_recycler_view)
        val addSourceButton = view.findViewById<Button>(R.id.add_source_button)
        val syncAllButton = view.findViewById<Button>(R.id.sync_all_button)

        // Set up RecyclerView
        dataSourceAdapter = DataSourceAdapter(dataSources)
        recyclerView.adapter = dataSourceAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Load sample data
        loadSampleDataSources()

        // Set up button listeners
        addSourceButton.setOnClickListener {
            // TODO: Open dialog to add new source
        }

        syncAllButton.setOnClickListener {
            // TODO: Trigger sync for all sources
        }

        // Update stats
        updateStats(view)
    }

    /**
     * Loads sample data sources for demonstration.
     */
    private fun loadSampleDataSources() {
        dataSources.add(
            DataSource(
                id = 1,
                name = "Community Spam Feed",
                type = "Remote URL",
                entriesCount = 127854,
                healthStatus = "Healthy",
                lastSynced = "1m ago",
                isEnabled = true
            )
        )
        dataSources.add(
            DataSource(
                id = 2,
                name = "Personal Block List",
                type = "Local CSV",
                entriesCount = 24610,
                healthStatus = "Healthy",
                lastSynced = "5m ago",
                isEnabled = true
            )
        )
        dataSources.add(
            DataSource(
                id = 3,
                name = "Telemarketer Database",
                type = "Remote URL",
                entriesCount = 212331,
                healthStatus = "Healthy",
                lastSynced = "3m ago",
                isEnabled = true
            )
        )
        dataSources.add(
            DataSource(
                id = 4,
                name = "Allow List (Whitelist)",
                type = "Local CSV",
                entriesCount = 1204,
                healthStatus = "Healthy",
                lastSynced = "1m ago",
                isEnabled = true
            )
        )
        dataSources.add(
            DataSource(
                id = 5,
                name = "User Reports Feed",
                type = "Remote URL",
                entriesCount = 46122,
                healthStatus = "Error",
                lastSynced = "23m ago",
                isEnabled = true
            )
        )
        dataSources.add(
            DataSource(
                id = 6,
                name = "Archived Spam List",
                type = "Local CSV",
                entriesCount = 856,
                healthStatus = "Disabled",
                lastSynced = "Never",
                isEnabled = false
            )
        )
        dataSourceAdapter.notifyDataSetChanged()
    }

    /**
     * Updates the stats displayed at the top of the dashboard.
     */
    private fun updateStats(view: View) {
        val totalSourcesText = view.findViewById<TextView>(R.id.total_sources_text)
        val totalEntriesText = view.findViewById<TextView>(R.id.total_entries_text)
        val lastSyncText = view.findViewById<TextView>(R.id.last_sync_text)
        val blockedTodayText = view.findViewById<TextView>(R.id.blocked_today_text)
        val shieldStatusText = view.findViewById<TextView>(R.id.shield_status_text)

        // TODO: Fetch actual data from database
        totalSourcesText?.text = "8"
        totalEntriesText?.text = "412,587"
        lastSyncText?.text = "2m ago"
        blockedTodayText?.text = "128"
        shieldStatusText?.text = "ACTIVE"
    }
}
