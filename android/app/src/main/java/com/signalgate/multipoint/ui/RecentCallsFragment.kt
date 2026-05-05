package com.signalgate.multipoint.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.signalgate.multipoint.R
import com.signalgate.multipoint.db.AppDatabase

class RecentCallsFragment : Fragment() {

    private lateinit var viewModel: RecentCallsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var adapter: RecentCallsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recent_calls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = AppDatabase.getDatabase(requireContext())
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return RecentCallsViewModel(database) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(RecentCallsViewModel::class.java)

        recyclerView = view.findViewById(R.id.recyclerView)
        emptyStateTextView = view.findViewById(R.id.emptyState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = RecentCallsAdapter(
            onBlockClick = { number ->
                viewModel.addBlockedNumber(number, null, false)
                Toast.makeText(requireContext(), "Number blocked: $number", Toast.LENGTH_SHORT).show()
            },
            onWhitelistClick = { number ->
                viewModel.addToWhitelist(number)
                Toast.makeText(requireContext(), "Added to whitelist: $number", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = this@RecentCallsFragment.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.recentCalls.observe(viewLifecycleOwner) { calls ->
            adapter.submitList(calls)
            emptyStateTextView.visibility = if (calls.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
