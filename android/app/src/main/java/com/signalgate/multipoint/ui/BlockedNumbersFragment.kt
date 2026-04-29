package com.signalgate.multipoint.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R

class BlockedNumbersFragment : Fragment() {

    private lateinit var viewModel: BlockedNumbersViewModel
    private lateinit var adapter: BlockedNumbersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blocked_numbers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(BlockedNumbersViewModel::class.java)

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewBlockedNumbers)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = BlockedNumbersAdapter { entry ->
            viewModel.deleteBlockedNumber(entry)
        }
        recyclerView.adapter = adapter

        viewModel.blockedNumbers.observe(viewLifecycleOwner) { blockedNumbers ->
            adapter.submitList(blockedNumbers)
        }

        view.findViewById<Button>(R.id.addBlockedNumberButton).setOnClickListener {
            showAddBlockedNumberDialog()
        }
    }

    private fun showAddBlockedNumberDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_blocked_number, null)
        val phoneNumberEditText = dialogView.findViewById<EditText>(R.id.phoneNumberEditText)
        val labelEditText = dialogView.findViewById<EditText>(R.id.labelEditText)
        val isPatternSwitch = dialogView.findViewById<Switch>(R.id.isPatternSwitch)

        AlertDialog.Builder(context)
            .setTitle("Add Blocked Number")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val phoneNumber = phoneNumberEditText.text.toString()
                val label = labelEditText.text.toString().ifEmpty { null }
                val isPattern = isPatternSwitch.isChecked

                if (phoneNumber.isNotBlank()) {
                    viewModel.addBlockedNumber(phoneNumber, label, isPattern)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
