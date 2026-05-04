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
import com.signalgate.multipoint.db.BlockEntry

class BlockedNumbersFragment : Fragment() {

    private lateinit var viewModel: BlockedNumbersViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlockedNumbersAdapter
    private lateinit var emptyStateTextView: TextView
    private lateinit var addButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_blocked_numbers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FIXED: Provide database to ViewModel
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

    // ... (rest of the file stays exactly the same)
    private fun setupRecyclerView() {
        adapter = BlockedNumbersAdapter(
            onDeleteClick = { entry ->
                viewModel.deleteBlockedNumber(entry)
                showUndoMessage(entry.phoneNumber)
            },
            onWhitelistClick = { entry ->
                viewModel.addToWhitelist(entry.phoneNumber)
                Toast.makeText(requireContext(), "Added to whitelist: ${entry.phoneNumber}", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BlockedNumbersFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.blockedNumbers.observe(viewLifecycleOwner) { entries ->
            adapter.submitList(entries)
            emptyStateTextView.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.actionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearActionResult()
            }
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
            .setMessage("Enter phone number or pattern to block")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val phoneNumber = phoneNumberEditText.text.toString().trim()
                val label = labelEditText.text.toString().trim().ifEmpty { null }
                val isPattern = isPatternSwitch.isChecked

                if (phoneNumber.isNotEmpty()) {
                    if (isPattern) {
                        viewModel.addPatternRule(phoneNumber, label)
                    } else {
                        viewModel.addBlockedNumber(phoneNumber, label, isPattern)
                    }
                } else {
                    Toast.makeText(requireContext(), "Phone number cannot be empty", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun showUndoMessage(number: String) {
        Toast.makeText(
            requireContext(),
            "Blocked: $number. Check recent actions to undo",
            Toast.LENGTH_LONG
        ).show()
    }
}
