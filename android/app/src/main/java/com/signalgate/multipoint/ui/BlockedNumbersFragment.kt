package com.signalgate.multipoint.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R
import com.signalgate.multipoint.db.AppDatabase

class BlockedNumbersFragment : Fragment() {

    private lateinit var viewModel: BlockedNumbersViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var addButton: Button
    private lateinit var adapter: BlockedNumbersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_blocked_numbers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return BlockedNumbersViewModel(database) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(BlockedNumbersViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerViewBlockedNumbers)
        emptyStateTextView = view.findViewById(R.id.emptyState)
        addButton = view.findViewById(R.id.addBlockedNumberButton)

        setupRecyclerView()
        setupObservers()
        setupAddButton()
    }

    private fun setupRecyclerView() {
        adapter = BlockedNumbersAdapter(
            onDeleteClick = { entry ->
                viewModel.deleteBlockedNumber(entry)
            },
            onWhitelistClick = { entry ->
                viewModel.addToWhitelist(entry.phoneNumber)
                Toast.makeText(requireContext(), "Added to whitelist: ${entry.phoneNumber}", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@BlockedNumbersFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.blockedNumbers.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            emptyStateTextView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupAddButton() {
        addButton.setOnClickListener {
            showAddBlockedNumberDialog()
        }
    }

    private fun showAddBlockedNumberDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_blocked_number, null)

        val phoneNumberEditText: EditText = dialogView.findViewById(R.id.phoneNumberEditText)
        val labelEditText: EditText = dialogView.findViewById(R.id.labelEditText)
        val isPatternSwitch: Switch = dialogView.findViewById(R.id.isPatternSwitch)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Blocked Number")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val phoneNumber = phoneNumberEditText.text.toString().trim()
                val label = labelEditText.text.toString().trim().ifEmpty { null }
                val isPattern = isPatternSwitch.isChecked

                if (phoneNumber.isNotEmpty()) {
                    viewModel.addBlockedNumber(phoneNumber, label, isPattern)
                    Toast.makeText(requireContext(), "Blocked added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
