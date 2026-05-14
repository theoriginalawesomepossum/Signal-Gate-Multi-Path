package com.signalgate.multipoint.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.signalgate.multipoint.R
import com.signalgate.multipoint.CrashLogActivity
import com.signalgate.multipoint.LogcatReaderActivity

class SettingsFragment : Fragment() {

    private lateinit var viewCrashLogsButton: MaterialButton
    private lateinit var viewDebugLogsButton: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewCrashLogsButton = view.findViewById(R.id.viewCrashLogsButton)
        viewDebugLogsButton = view.findViewById(R.id.viewDebugLogsButton)

        setupCrashLogButton()
        setupDebugLogButton()
    }

    private fun setupCrashLogButton() {
        viewCrashLogsButton.setOnClickListener {
            startActivity(Intent(requireContext(), CrashLogActivity::class.java))
        }
    }

    private fun setupDebugLogButton() {
        viewDebugLogsButton.setOnClickListener {
            startActivity(Intent(requireContext(), LogcatReaderActivity::class.java))
        }
    }
}
