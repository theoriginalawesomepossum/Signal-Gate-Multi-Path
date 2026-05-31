package com.signalgate.multipoint.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.signalgate.multipoint.R
import com.signalgate.multipoint.models.PermissionItem

/**
 * PermissionsAdapter displays a list of app permissions with toggle switches.
 * Reuses the visual style of DataSourceAdapter for consistency.
 */
class PermissionsAdapter(
    private val permissions: List<PermissionItem>,
    private val onPermissionToggled: (PermissionItem, Boolean) -> Unit
) : RecyclerView.Adapter<PermissionsAdapter.PermissionViewHolder>() {

    inner class PermissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.source_name_text)
        private val descriptionText: TextView = itemView.findViewById(R.id.source_type_text)
        private val statusText: TextView = itemView.findViewById(R.id.health_status_text)
        private val detailText: TextView = itemView.findViewById(R.id.health_detail_text)
        private val toggleSwitch: SwitchMaterial = itemView.findViewById(R.id.enable_switch)
        private val ledIndicator: View = itemView.findViewById(R.id.source_led_indicator)
        private val iconView: ImageView = itemView.findViewById(R.id.source_icon)
        
        // Hide unused views from item_data_source
        init {
            itemView.findViewById<View>(R.id.entries_count_text).visibility = View.GONE
            itemView.findViewById<View>(R.id.last_synced_text).visibility = View.GONE
            itemView.findViewById<View>(R.id.sync_button).visibility = View.GONE
            itemView.findViewById<View>(R.id.more_button).visibility = View.GONE
        }

        fun bind(permission: PermissionItem) {
            nameText.text = permission.name
            descriptionText.text = permission.description
            
            iconView.setImageResource(android.R.drawable.ic_lock_idle_lock)
            
            updateUiState(permission.isGranted)
            
            toggleSwitch.setOnCheckedChangeListener(null)
            toggleSwitch.isChecked = permission.isGranted
            toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
                updateUiState(isChecked)
                onPermissionToggled(permission, isChecked)
            }
        }

        private fun updateUiState(isGranted: Boolean) {
            if (isGranted) {
                ledIndicator.setBackgroundResource(R.drawable.bg_led_indicator)
                ledIndicator.background.setTint(itemView.context.getColor(R.color.neon_blue))
                statusText.text = "Granted"
                detailText.text = "Active"
                statusText.setTextColor(itemView.context.getColor(R.color.status_low))
            } else {
                ledIndicator.setBackgroundResource(R.drawable.bg_led_indicator)
                ledIndicator.background.setTint(itemView.context.getColor(R.color.text_muted))
                statusText.text = "Denied"
                detailText.text = "Disabled"
                statusText.setTextColor(itemView.context.getColor(R.color.text_muted))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_source, parent, false)
        return PermissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: PermissionViewHolder, position: Int) {
        holder.bind(permissions[position])
    }

    override fun getItemCount(): Int = permissions.size
}
