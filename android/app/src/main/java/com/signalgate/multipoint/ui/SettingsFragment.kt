package com.signalgate.multipoint.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.signalgate.multipoint.R

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var previewShield: View
    private lateinit var redSeekBar: SeekBar
    private lateinit var greenSeekBar: SeekBar
    private lateinit var blueSeekBar: SeekBar
    private lateinit var applyButton: Button
    private lateinit var previewButton: Button

    private var currentRed = 66
    private var currentGreen = 133
    private var currentBlue = 244

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        previewShield = view.findViewById(R.id.previewShield)
        redSeekBar = view.findViewById(R.id.redSeekBar)
        greenSeekBar = view.findViewById(R.id.greenSeekBar)
        blueSeekBar = view.findViewById(R.id.blueSeekBar)
        applyButton = view.findViewById(R.id.applyButton)
        previewButton = view.findViewById(R.id.previewButton)

        loadSavedColors()
        setupSeekBars()
        setupButtons()
    }

    private fun loadSavedColors() {
        currentRed = sharedPreferences.getInt("shield_red", 66)
        currentGreen = sharedPreferences.getInt("shield_green", 133)
        currentBlue = sharedPreferences.getInt("shield_blue", 244)

        redSeekBar.progress = currentRed
        greenSeekBar.progress = currentGreen
        blueSeekBar.progress = currentBlue
        updatePreview()
    }

    private fun setupSeekBars() {
        redSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentRed = progress
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        greenSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentGreen = progress
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        blueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentBlue = progress
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updatePreview() {
        previewShield.setBackgroundColor(
            android.graphics.Color.rgb(currentRed, currentGreen, currentBlue)
        )
    }

    private fun setupButtons() {
        previewButton.setOnClickListener {
            updatePreview()
        }

        applyButton.setOnClickListener {
            saveColors()
            Toast.makeText(
                requireContext(),
                "Shield color updated!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveColors() {
        with(sharedPreferences.edit()) {
            putInt("shield_red", currentRed)
            putInt("shield_green", currentGreen)
