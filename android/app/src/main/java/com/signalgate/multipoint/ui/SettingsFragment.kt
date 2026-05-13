package com.signalgate.multipoint.ui

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.signalgate.multipoint.R

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var previewShield: View

    private lateinit var redSlider: Slider
    private lateinit var greenSlider: Slider
    private lateinit var blueSlider: Slider

    private lateinit var previewButton: MaterialButton
    private lateinit var applyButton: MaterialButton

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

        // Ensure shield preview stays visible
        previewShield.minimumHeight = 120

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

        val listener = Slider.OnChangeListener { slider, value, fromUser ->
            when (slider.id) {
                R.id.redSeekBar -> currentRed = value.toInt()
                R.id.greenSeekBar -> currentGreen = value.toInt()
                R.id.blueSeekBar -> currentBlue = value.toInt()
            }
            // Live preview while dragging
            updatePreview()
        }

        redSlider.addOnChangeListener(listener)
        greenSlider.addOnChangeListener(listener)
        blueSlider.addOnChangeListener(listener)
    }

    private fun updatePreview() {

        val color =
            android.graphics.Color.rgb(
                currentRed,
                currentGreen,
                currentBlue
            )

        previewShield.setBackgroundColor(color)

        previewShield.invalidate()
    }

    private fun setupButtons() {

        previewButton.setOnClickListener {

            updatePreview()

            Toast.makeText(
                requireContext(),
                "Preview updated",
                Toast.LENGTH_SHORT
            ).show()
        }

        applyButton.setOnClickListener {

            saveColors()

            Toast.makeText(
                requireContext(),
                "Theme color saved",
                Toast.LENGTH_SHORT
            ).show()

            // Restart required popup
            AlertDialog.Builder(requireContext())
                .setTitle("Restart Recommended")
                .setMessage(
                    "Your new theme color has been saved.\n\n" +
                    "Some UI elements may require an app restart " +
                    "to fully update."
                )
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun saveColors() {

        with(sharedPreferences.edit()) {

            putInt(
                "shield_red",
                currentRed
            )

            putInt(
                "shield_green",
                currentGreen
            )

            putInt(
                "shield_blue",
                currentBlue
            )

            apply()
        }
    }
}
