package com.signalgate.multipoint.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
    private lateinit var adapter: BlockedNumbersAdapter
    private var addButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_blocked_numbers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Safe ViewModel setup
        val database = AppDatabase.getDatabase(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return BlockedNumbersViewModel(database) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(BlockedNumbersViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerViewBlockedNumbers)
        addButton = view.findViewById(R.id.addBlockedNumberButton)

        setupRecyclerView()
        setupObservers()

        addButton?.setOnClickListener {
            Toast.makeText(requireContext(), "Add blocked number coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = BlockedNumbersAdapter(
            onDeleteClick = { entry ->
                viewModel.deleteBlockedNumber(entry)
            },
            onWhitelistClick = { entry ->
                viewModel.addToWhitelist(entry.phoneNumber)
                Toast.makeText(requireContext(), "Whitelisted: ${entry.phoneNumber}", Toast.LENGTH_SHORT).show()
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
        }
    }
}
