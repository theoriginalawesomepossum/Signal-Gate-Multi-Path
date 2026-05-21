package com.signalgate.multipoint.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R
import com.signalgate.multipoint.models.DataSource

/**
 * DataSourceAdapter displays a list of data sources in a RecyclerView.
 */
class DataSourceAdapter(private val dataSources: List<DataSource>) :
    RecyclerView.Adapter<DataSourceAdapter.DataSourceViewHolder>() {

    inner class DataSourceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sourceNameText: TextView = itemView.findViewById(R.id.source_name_text)
        private val sourceTypeText: TextView = itemView.findViewById(R.id.source_type_text)
        private val entriesCountText: TextView = itemView.findViewById(R.id.entries_count_text)
        private val healthStatusText: TextView = itemView.findViewById(R.id.health_status_text)
        private val lastSyncedText: TextView = itemView.findViewById(R.id.last_synced_text)
        private val enableSwitch: Switch = itemView.findViewById(R.id.enable_switch)
        private val syncButton: Button = itemView.findViewById(R.id.sync_button)
        private val moreButton: Button = itemView.findViewById(R.id.more_button)
        private val healthIcon: ImageView = itemView.findViewById(R.id.health_icon)

        fun bind(dataSource: DataSource) {
            sourceNameText.text = dataSource.name
            sourceTypeText.text = dataSource.type
            entriesCountText.text = "${dataSource.entriesCount} Entries"
            lastSyncedText.text = "Last synced: ${dataSource.lastSynced}"
            enableSwitch.isChecked = dataSource.isEnabled

            // Set health status color and text
            when (dataSource.healthStatus) {
                "Healthy" -> {
                    healthStatusText.text = "Healthy"
                    healthStatusText.setTextColor(itemView.context.getColor(R.color.status_low))
                    healthIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                    healthIcon.setColorFilter(itemView.context.getColor(R.color.status_low))
                }
                "Error" -> {
                    healthStatusText.text = "Error"
                    healthStatusText.setTextColor(itemView.context.getColor(R.color.status_high))
                    healthIcon.setImageResource(android.R.drawable.ic_dialog_alert)
                    healthIcon.setColorFilter(itemView.context.getColor(R.color.status_high))
                }
                "Disabled" -> {
                    healthStatusText.text = "Disabled"
                    healthStatusText.setTextColor(itemView.context.getColor(R.color.text_muted))
                    healthIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                    healthIcon.setColorFilter(itemView.context.getColor(R.color.text_muted))
                }
            }

            // Set up button listeners
            syncButton.setOnClickListener {
                // TODO: Implement sync logic for this source
            }

            moreButton.setOnClickListener {
                // TODO: Show more options (edit, delete, etc.)
            }

            enableSwitch.setOnCheckedChangeListener { _, isChecked ->
                // TODO: Update source enabled status in database
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
