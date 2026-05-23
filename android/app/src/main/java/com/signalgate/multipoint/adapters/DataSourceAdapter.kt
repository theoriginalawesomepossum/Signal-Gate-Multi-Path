package com.signalgate.multipoint.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.signalgate.multipoint.R
import com.signalgate.multipoint.models.DataSource
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * DataSourceAdapter displays a list of data sources in a RecyclerView.
 * Integrates with DashboardViewModel for state management and LED indicator logic.
 */
class DataSourceAdapter(
    private val dataSources: List<DataSource>,
    private val viewModel: DashboardViewModel? = null,
    private val onSourceToggled: ((sourceId: Int, isEnabled: Boolean) -> Unit)? = null,
    private val onSyncClicked: ((sourceId: Int) -> Unit)? = null,
    private val onSettingsClicked: ((sourceId: Int) -> Unit)? = null
) : RecyclerView.Adapter<DataSourceAdapter.DataSourceViewHolder>() {

    inner class DataSourceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sourceNameText: TextView = itemView.findViewById(R.id.source_name_text)
        private val sourceTypeText: TextView = itemView.findViewById(R.id.source_type_text)
        private val entriesCountText: TextView = itemView.findViewById(R.id.entries_count_text)
        private val healthStatusText: TextView = itemView.findViewById(R.id.health_status_text)
        private val healthDetailText: TextView = itemView.findViewById(R.id.health_detail_text)
        private val lastSyncedText: TextView = itemView.findViewById(R.id.last_synced_text)
        private val enableSwitch: SwitchMaterial = itemView.findViewById(R.id.enable_switch)
        private val syncButton: ImageView = itemView.findViewById(R.id.sync_button)
        private val moreButton: ImageView = itemView.findViewById(R.id.more_button)
        private val sourceIcon: ImageView = itemView.findViewById(R.id.source_icon)
        private val ledIndicator: View = itemView.findViewById(R.id.source_led_indicator)

        fun bind(dataSource: DataSource) {
            sourceNameText.text = dataSource.name
            sourceTypeText.text = dataSource.type
            
            // Format entries count with commas
            val formattedCount = NumberFormat.getNumberInstance(Locale.US).format(dataSource.entriesCount)
            entriesCountText.text = formattedCount
            
            lastSyncedText.text = dataSource.lastSynced
            
            // Set icon based on type
            when (dataSource.type) {
                "Remote URL" -> sourceIcon.setImageResource(android.R.drawable.ic_menu_share)
                "Local CSV" -> sourceIcon.setImageResource(android.R.drawable.ic_menu_save)
                "Local XLSX" -> sourceIcon.setImageResource(android.R.drawable.ic_menu_save)
                else -> sourceIcon.setImageResource(android.R.drawable.ic_menu_help)
            }

            // Initial state
            updateUiState(dataSource.isEnabled)
            
            // Set up switch listener
            enableSwitch.setOnCheckedChangeListener(null) // Clear listener to avoid recursive calls
            enableSwitch.isChecked = dataSource.isEnabled
            enableSwitch.setOnCheckedChangeListener { _, isChecked ->
                updateUiState(isChecked)
                onSourceToggled?.invoke(dataSource.id, isChecked)
                viewModel?.toggleSourceEnabled(dataSource.id, isChecked)
            }

            // Set up button listeners
            syncButton.setOnClickListener {
                onSyncClicked?.invoke(dataSource.id)
                viewModel?.syncSource(dataSource.id)
            }

            moreButton.setOnClickListener {
                onSettingsClicked?.invoke(dataSource.id)
                // TODO: Show settings dialog or bottom sheet
            }
        }

        private fun updateUiState(isEnabled: Boolean) {
            if (isEnabled) {
                // LED ON - Blue color
                ledIndicator.setBackgroundResource(R.drawable.bg_led_indicator)
                ledIndicator.background.setTint(itemView.context.getColor(R.color.neon_blue))
                healthStatusText.text = "Active"
                healthDetailText.text = "Enabled"
                healthStatusText.setTextColor(itemView.context.getColor(R.color.status_low))
            } else {
                // LED OFF - Gray/muted color
                ledIndicator.setBackgroundResource(R.drawable.bg_led_indicator)
                ledIndicator.background.setTint(itemView.context.getColor(R.color.text_muted))
                healthStatusText.text = "Inactive"
                healthDetailText.text = "Disabled"
                healthStatusText.setTextColor(itemView.context.getColor(R.color.text_muted))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataSourceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_source, parent, false)
        return DataSourceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DataSourceViewHolder, position: Int) {
        holder.bind(dataSources[position])
    }

    override fun getItemCount(): Int = dataSources.size
}
