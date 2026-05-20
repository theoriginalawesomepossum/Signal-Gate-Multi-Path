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
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.signalgate.multipoint.CallScreeningService.CallDecision
import com.signalgate.multipoint.R

const val ACTION_OVERLAY_ALLOW = "com.signalgate.multipoint.OVERLAY_ALLOW"
const val ACTION_OVERLAY_SCREEN = "com.signalgate.multipoint.OVERLAY_SCREEN"
const val ACTION_OVERLAY_BLOCK = "com.signalgate.multipoint.OVERLAY_BLOCK"

class CallOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var currentCallInfo: CallInfo? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        initializeOverlay()
    }

    private fun initializeOverlay() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.call_shield_overlay, null)

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

    private fun setupButtonListeners() {
        overlayView?.apply {
            findViewById<Button>(R.id.allow_button)?.setOnClickListener { handleAllowAction() }
            findViewById<Button>(R.id.screen_button)?.setOnClickListener { handleScreenAction() }
            findViewById<Button>(R.id.block_button)?.setOnClickListener { handleBlockAction() }
            findViewById<Button>(R.id.more_details_button)?.setOnClickListener { toggleMoreDetails() }
            findViewById<ImageButton>(R.id.accept_call_button)?.setOnClickListener { handleAllowAction() }
            findViewById<ImageButton>(R.id.reject_call_button)?.setOnClickListener { handleBlockAction() }
        }
    }

    private fun handleAllowAction() {
        currentCallInfo?.let { sendActionBroadcast(ACTION_OVERLAY_ALLOW, it) }
        removeOverlay()
    }

    private fun handleScreenAction() {
        currentCallInfo?.let { sendActionBroadcast(ACTION_OVERLAY_SCREEN, it) }
        removeOverlay()
    }

    private fun handleBlockAction() {
        currentCallInfo?.let { sendActionBroadcast(ACTION_OVERLAY_BLOCK, it) }
        removeOverlay()
    }

    private fun sendActionBroadcast(action: String, callInfo: CallInfo) {
        val intent = Intent(action)
        intent.putExtra("call_info", callInfo)
        sendBroadcast(intent)
    }

    private fun toggleMoreDetails() {
        overlayView?.findViewById<LinearLayout>(R.id.details_container)?.let {
            it.visibility = if (it.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        currentCallInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("call_info", CallInfo::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra("call_info")
        }

        currentCallInfo?.let { updateOverlayUI(it) }
            ?: run {
                val number = intent?.getStringExtra("incoming_number") ?: "Unknown"
                updateOverlayUILegacy(number, "Unknown Status")
            }

        return START_NOT_STICKY
    }

    private fun updateOverlayUI(callInfo: CallInfo) {
        overlayView?.let { view ->
            view.findViewById<TextView>(R.id.caller_number_text_view)?.text = callInfo.originalPhoneNumber
            view.findViewById<TextView>(R.id.spam_status_text_view)?.text = callInfo.spamStatus
            view.findViewById<TextView>(R.id.spam_category_text_view)?.text = callInfo.spamCategory ?: "Unknown"

            callInfo.confidence?.let { conf ->
                view.findViewById<TextView>(R.id.confidence_text_view)?.text = "Confidence: $conf%"
                view.findViewById<ProgressBar>(R.id.confidence_progress_bar)?.progress = conf
            }

            callInfo.riskLevel?.let { risk ->
                view.findViewById<TextView>(R.id.risk_level_text_view)?.text = risk
            }

            val sourcesCount = callInfo.matchedSources.size
            view.findViewById<TextView>(R.id.matched_sources_label)?.text = "Matched in $sourcesCount sources"

            populateSourceTags(view, callInfo.matchedSources)
        }
    }

    private fun updateOverlayUILegacy(incomingNumber: String, spamStatus: String) {
        overlayView?.let { view ->
            view.findViewById<TextView>(R.id.caller_number_text_view)?.text = incomingNumber
            view.findViewById<TextView>(R.id.spam_status_text_view)?.text = spamStatus
        }
    }

    private fun populateSourceTags(view: View, sources: List<String>) {
        val container = view.findViewById<LinearLayout>(R.id.source_tags_container) ?: return
        container.removeAllViews()

        for (source in sources) {
            val tag = TextView(this).apply {
                text = source
                setTextAppearance(R.style.TextAppearance_SignalGate_Muted)
                setBackgroundResource(R.drawable.tag_background)
                setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dpToPx(8) }
            }
            container.addView(tag)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun removeOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
        stopSelf()
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }
}
