package com.signalgate.multipoint.ui

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.slider.Slider
import com.signalgate.multipoint.PostCallNotifier
import com.signalgate.multipoint.R

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var previewShield: View

    private lateinit var redSlider: Slider
    private lateinit var greenSlider: Slider
    private lateinit var blueSlider: Slider

    private lateinit var previewButton: Button
    private lateinit var applyButton: Button
    
    private lateinit var checkPermissionsButton: Button
    private lateinit var viewLogsButton: Button
    private lateinit var testPopupButton: Button

    private var currentRed = 66
    private var currentGreen = 133
    private var currentBlue = 244

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_settings,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                requireContext()
            )

        previewShield =
            view.findViewById(R.id.previewShield)

        redSlider =
            view.findViewById(R.id.redSeekBar)

        greenSlider =
            view.findViewById(R.id.greenSeekBar)

        blueSlider =
            view.findViewById(R.id.blueSeekBar)

        previewButton =
            view.findViewById(R.id.previewButton)

        applyButton =
            view.findViewById(R.id.applyButton)
            
        checkPermissionsButton =
            view.findViewById(R.id.checkPermissionsButton)
            
        viewLogsButton =
            view.findViewById(R.id.viewLogsButton)
            
        testPopupButton =
            view.findViewById(R.id.testPopupButton)

        loadSavedColors()

        setupSliders()

        setupButtons()
    }

    private fun loadSavedColors() {

        currentRed =
            sharedPreferences.getInt(
                "shield_red",
                66
            )

        currentGreen =
            sharedPreferences.getInt(
                "shield_green",
                133
            )

        currentBlue =
            sharedPreferences.getInt(
                "shield_blue",
                244
            )

        redSlider.value = currentRed.toFloat()
        greenSlider.value = currentGreen.toFloat()
        blueSlider.value = currentBlue.toFloat()

        updatePreview()
    }

    private fun setupSliders() {

        redSlider.addOnChangeListener { _, value, _ ->
            currentRed = value.toInt()
            updatePreview()
        }

        greenSlider.addOnChangeListener { _, value, _ ->
            currentGreen = value.toInt()
            updatePreview()
        }

        blueSlider.addOnChangeListener { _, value, _ ->
            currentBlue = value.toInt()
            updatePreview()
        }
    }

    private fun updatePreview() {

        val color =
            android.graphics.Color.rgb(
                currentRed,
                currentGreen,
                currentBlue
            )

        previewShield.setBackgroundColor(color)
    }

    private fun setupButtons() {

        previewButton.setOnClickListener {
            updatePreview()
            Toast.makeText(requireContext(), "Preview updated", Toast.LENGTH_SHORT).show()
        }

        applyButton.setOnClickListener {
            saveColors()
            Toast.makeText(requireContext(), "Theme color saved", Toast.LENGTH_SHORT).show()
            
            AlertDialog.Builder(requireContext())
                .setTitle("Restart Recommended")
                .setMessage("Your new theme color has been saved.\n\nSome UI elements may require an app restart to fully update.")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
        
        checkPermissionsButton.setOnClickListener {
            requestSpecialPermissions()
        }
        
        viewLogsButton.setOnClickListener {
            showLogsInfo()
        }
        
        testPopupButton.setOnClickListener {
            PostCallNotifier.show(requireContext(), "555-0123")
            Toast.makeText(requireContext(), "Test notification sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestSpecialPermissions() {
        // Overlay Permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${requireContext().packageName}")
            )
            startActivity(intent)
            Toast.makeText(requireContext(), "Please enable 'Display over other apps'", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Overlay permission already granted", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showLogsInfo() {
        AlertDialog.Builder(requireContext())
            .setTitle("System Logs")
            .setMessage("SignalGate logs events to the Android System Log (Logcat).\n\nFilter by tag: 'SignalGateScreening' or 'PhoneStateReceiver' to see call events.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun saveColors() {
        with(sharedPreferences.edit()) {
            putInt("shield_red", currentRed)
            putInt("shield_green", currentGreen)
            putInt("shield_blue", currentBlue)
            apply()
        }
    }
}
