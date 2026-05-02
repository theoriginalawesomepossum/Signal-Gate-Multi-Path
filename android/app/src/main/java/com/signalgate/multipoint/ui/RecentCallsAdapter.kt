package com.signalgate.multipoint.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R
import com.signalgate.multipoint.db.CallLogEntry
import com.signalgate.multipoint.utils.PhoneNumberUtils

class RecentCallsAdapter(
    private val onBlockClick: (String) -> Unit,
    private val onWhitelistClick: (String) -> Unit
) : ListAdapter<CallLogEntry, RecentCallsAdapter.CallViewHolder>(CallLogDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_call, parent, false)
        return CallViewHolder(view)
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CallViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val phoneNumberText: TextView = itemView.findViewById(R.id.phoneNumberText)
        private val decisionText: TextView = itemView.findViewById(R.id.decisionText)
        private val blockButton: Button = itemView.findViewById(R.id.blockButton)
        private val whitelistButton: Button = itemView.findViewById(R.id.whitelistButton)

        fun bind(call: CallLogEntry) {
            val displayNumber = PhoneNumberUtils.formatPhoneNumberForDisplay(call.phoneNumber)
            phoneNumberText.text = displayNumber
            decisionText.text = "Decision: ${call.decision}"

            blockButton.setOnClickListener { onBlockClick(call.phoneNumber) }
            whitelistButton.setOnClickListener { onWhitelistClick(call.phoneNumber) }
        }
    }

    private class CallDiffCallback : DiffUtil.ItemCallback<CallLogEntry>() {
        override fun areItemsTheSame(oldItem: CallLogEntry, newItem: CallLogEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CallLogEntry, newItem: CallLogEntry): Boolean {
            return oldItem == newItem
        }
    }
}
