package com.signalgate.multipoint

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.signalgate.multipoint.R

/**
 * CallOverlayService displays a transparent, glassmorphic overlay on incoming calls.
 * It dynamically populates UI elements based on CallInfo data and provides interactive buttons
 * for user actions (Allow, Screen, Block).
 */
class CallOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentCallInfo: CallInfo? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        initializeOverlay()
    }

    /**
     * Initializes the overlay view and adds it to the window manager.
     */
    private fun initializeOverlay() {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = layoutInflater.inflate(R.layout.call_shield_overlay, null)

        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            )
        }

        windowManager?.addView(overlayView, params)
        setupButtonListeners()
    }

    /**
     * Sets up click listeners for the action buttons.
     */
    private fun setupButtonListeners() {
        overlayView?.findViewById<Button>(R.id.allow_button)?.setOnClickListener {
            handleAllowAction()
        }

        overlayView?.findViewById<Button>(R.id.screen_button)?.setOnClickListener {
            handleScreenAction()
        }

        overlayView?.findViewById<Button>(R.id.block_button)?.setOnClickListener {
            handleBlockAction()
        }

        overlayView?.findViewById<Button>(R.id.more_details_button)?.setOnClickListener {
            toggleMoreDetails()
        }
    }

    /**
     * Handles the Allow action: allows the call to proceed.
     */
    private fun handleAllowAction() {
        // TODO: Implement logic to allow the call
        // This would typically involve:
        // 1. Sending a decision to the CallScreeningService
        // 2. Logging the action to the database
        // 3. Dismissing the overlay
        removeOverlay()
    }

    /**
     * Handles the Screen action: screens the call (sends to voicemail or shows notification).
     */
    private fun handleScreenAction() {
        // TODO: Implement logic to screen the call
        // This would typically involve:
        // 1. Sending a decision to the CallScreeningService
        // 2. Logging the action to the database
        // 3. Dismissing the overlay
        removeOverlay()
    }

    /**
     * Handles the Block action: blocks the call.
     */
    private fun handleBlockAction() {
        // TODO: Implement logic to block the call
        // This would typically involve:
        // 1. Sending a decision to the CallScreeningService
        // 2. Logging the action to the database
        // 3. Dismissing the overlay
        removeOverlay()
    }

    /**
     * Toggles the visibility of the "More Details" section.
     */
    private fun toggleMoreDetails() {
        // TODO: Implement expandable details section
        // This would show additional information like:
        // 1. Full list of matched sources
        // 2. Call history with this number
        // 3. Detailed risk analysis
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Extract CallInfo from the intent
        val callInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("call_info", CallInfo::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("call_info")
        }

        if (callInfo != null) {
            currentCallInfo = callInfo
            updateOverlayUI(callInfo)
        } else {
            // Fallback to legacy string-based data
            val incomingNumber = intent?.getStringExtra("incoming_number") ?: "Unknown"
            val spamStatus = intent?.getStringExtra("spam_status") ?: "Unknown Status"
            updateOverlayUILegacy(incomingNumber, spamStatus)
        }

        return START_NOT_STICKY
    }

    /**
     * Updates the overlay UI with the provided CallInfo data.
     */
    private fun updateOverlayUI(callInfo: CallInfo) {
        overlayView?.let { view ->
            // Update caller information
            view.findViewById<TextView>(R.id.caller_number_text_view)?.text = callInfo.originalPhoneNumber
            view.findViewById<TextView>(R.id.spam_status_text_view)?.text = callInfo.spamStatus
            view.findViewById<TextView>(R.id.spam_category_text_view)?.text = callInfo.spamCategory ?: "Unknown"

            // Update confidence
            callInfo.confidence?.let { confidence ->
                view.findViewById<TextView>(R.id.confidence_text_view)?.text = "Confidence: $confidence%"
                view.findViewById<ProgressBar>(R.id.confidence_progress_bar)?.progress = confidence
            }

            // Update risk level
            callInfo.riskLevel?.let { riskLevel ->
                view.findViewById<TextView>(R.id.risk_level_text_view)?.text = riskLevel
            }

            // Update matched sources
            val sourcesCount = callInfo.matchedSources.size
            view.findViewById<TextView>(R.id.matched_sources_label)?.text = "Matched in $sourcesCount sources"

            // Populate source tags dynamically
            populateSourceTags(view, callInfo.matchedSources)
        }
    }

    /**
     * Updates the overlay UI with legacy string-based data (fallback).
     */
    private fun updateOverlayUILegacy(incomingNumber: String, spamStatus: String) {
        overlayView?.let { view ->
            view.findViewById<TextView>(R.id.caller_number_text_view)?.text = incomingNumber
            view.findViewById<TextView>(R.id.spam_status_text_view)?.text = spamStatus
        }
    }

    /**
     * Dynamically populates the source tags container with tags for each matched source.
     */
    private fun populateSourceTags(view: View, sources: List<String>) {
        val sourceTagsContainer = view.findViewById<LinearLayout>(R.id.source_tags_container)
        sourceTagsContainer?.removeAllViews()

        for (source in sources) {
            val tagView = TextView(this)
            tagView.text = source
            tagView.setTextAppearance(R.style.TextAppearance_SignalGate_Muted)
            tagView.setBackgroundResource(R.drawable.tag_background)
            tagView.setPadding(
                dpToPx(8),
                dpToPx(4),
                dpToPx(8),
                dpToPx(4)
            )

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginEnd = dpToPx(8)
            tagView.layoutParams = layoutParams

            sourceTagsContainer?.addView(tagView)
        }
    }

    /**
     * Converts dp to pixels for use in UI layout calculations.
     */
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    /**
     * Removes the overlay from the window manager.
     */
    private fun removeOverlay() {
        if (overlayView != null && windowManager != null) {
            windowManager?.removeView(overlayView)
            overlayView = null
        }
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
