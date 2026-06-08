package com.signalgate.multipoint.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity

class BlockedNumbersAdapter(
    private val onDeleteClick: (UnifiedEntryEntity) -> Unit,
    private val onWhitelistClick: (UnifiedEntryEntity) -> Unit
) : ListAdapter<UnifiedEntryEntity, BlockedNumbersAdapter.BlockViewHolder>(BlockEntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_entry, parent, false)
        return BlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        private val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(entry: UnifiedEntryEntity) {
            phoneNumberTextView.text = entry.phoneNumber
            labelTextView.text = entry.category ?: if (entry.isPattern) "Pattern" else ""
            deleteButton.setOnClickListener { onDeleteClick(entry) }
        }
    }

    private class BlockEntryDiffCallback : DiffUtil.ItemCallback<UnifiedEntryEntity>() {
        override fun areItemsTheSame(oldItem: UnifiedEntryEntity, newItem: UnifiedEntryEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UnifiedEntryEntity, newItem: UnifiedEntryEntity): Boolean {
            return oldItem == newItem
        }
    }
}
