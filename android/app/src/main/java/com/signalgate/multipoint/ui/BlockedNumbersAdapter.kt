package com.signalgate.multipoint.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R
import com.signalgate.multipoint.db.BlockEntry

class BlockedNumbersAdapter(
    private val onDeleteClick: (BlockEntry) -> Unit
) : RecyclerView.Adapter<BlockedNumbersAdapter.BlockViewHolder>() {

    private var blockList: List<BlockEntry> = emptyList()

    fun submitList(newList: List<BlockEntry>) {
        blockList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_entry, parent, false)
        return BlockViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val entry = blockList[position]
        holder.bind(entry)
    }

    override fun getItemCount(): Int = blockList.size

    inner class BlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        private val labelTextView: TextView = itemView.findViewById(R.id.labelTextView)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        fun bind(entry: BlockEntry) {
            phoneNumberTextView.text = entry.phoneNumber
            labelTextView.text = entry.label ?: if (entry.isPattern) "Pattern" else ""
            deleteButton.setOnClickListener { onDeleteClick(entry) }
        }
    }
}
