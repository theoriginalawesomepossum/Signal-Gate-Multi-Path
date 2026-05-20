package com.signalgate.multipoint

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.material3.MaterialTheme
import com.signalgate.multipoint.ui.theme.SignalGateTheme

class CallOverlayServiceCompose : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var currentCallInfo: CallInfo? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        initializeOverlay()
    }

    private fun initializeOverlay() {
        overlayView = ComposeView(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                SignalGateTheme {
                    CallShieldOverlay(
                        onAllowClick = { handleAllowAction() },
                        onScreenClick = { handleScreenAction() },
                        onBlockClick = { handleBlockAction() }
                    )
                }
            }
        }

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
                // Handle legacy case if needed
            }

        return START_NOT_STICKY
    }

    private fun updateOverlayUI(callInfo: CallInfo) {
        // Update the Compose UI with call information
        overlayView?.setContent {
            SignalGateTheme {
                CallShieldOverlay(
                    phoneNumber = callInfo.originalPhoneNumber,
                    country = callInfo.country ?: "Unknown",
                    spamLabel = callInfo.spamStatus,
                    spamCategory = callInfo.spamCategory ?: "Unknown",
                    confidence = (callInfo.confidence?.toFloat() ?: 0f) / 100f,
                    riskLevel = callInfo.riskLevel ?: "UNKNOWN",
                    sourceTags = callInfo.matchedSources,
                    onAllowClick = { handleAllowAction() },
                    onScreenClick = { handleScreenAction() },
                    onBlockClick = { handleBlockAction() }
                )
            }
        }
    }

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
