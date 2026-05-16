package com.signalgate.multipoint.ui

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.signalgate.multipoint.R
import com.signalgate.multipoint.CrashLogActivity
import com.signalgate.multipoint.LogcatReaderActivity
import com.signalgate.multipoint.overlay.OverlayManagerService

class SettingsFragment : Fragment() {

    private lateinit var viewCrashLogsButton: MaterialButton
    private lateinit var viewDebugLogsButton: MaterialButton
    private lateinit var toggleOverlayButton: MaterialButton
    private lateinit var toggleScreeningButton: MaterialButton
    private lateinit var overlayLabel: TextView
    private lateinit var screeningLabel: TextView

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
        toggleOverlayButton = view.findViewById(R.id.toggleOverlayPermission)
        toggleScreeningButton = view.findViewById(R.id.toggleScreeningPermission)
        overlayLabel = view.findViewById(R.id.overlayPermissionLabel)
        screeningLabel = view.findViewById(R.id.screeningPermissionLabel)

        setupCrashLogButton()
        setupDebugLogButton()
        setupPermissionButtons()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
    }

    private fun setupPermissionButtons() {
        toggleOverlayButton.setOnClickListener {
            OverlayManagerService.openPermissionSettings(requireContext())
        }

        toggleScreeningButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = requireContext().getSystemService(Context.ROLE_SERVICE) as RoleManager
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                startActivity(intent)
            } else {
                // Fallback for older versions if needed, though most users will be on Q+
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                startActivity(intent)
            }
        }
    }

    private fun updatePermissionStatus() {
        // Overlay Status
        val hasOverlay = OverlayManagerService.checkOverlayPermission(requireContext())
        toggleOverlayButton.text = if (hasOverlay) "ENABLED" else "DISABLED"
        toggleOverlayButton.setTextColor(if (hasOverlay) 0xFF00C853.toInt() else 0xFFD50000.toInt())

        // Call Screening Status
        val hasScreening = isCallScreeningEnabled()
        toggleScreeningButton.text = if (hasScreening) "ENABLED" else "DISABLED"
        toggleScreeningButton.setTextColor(if (hasScreening) 0xFF00C853.toInt() else 0xFFD50000.toInt())
    }

    private fun isCallScreeningEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = requireContext().getSystemService(Context.ROLE_SERVICE) as RoleManager
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        } else {
            val telecomManager = requireContext().getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            requireContext().packageName == telecomManager.defaultDialerPackage
        }
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
