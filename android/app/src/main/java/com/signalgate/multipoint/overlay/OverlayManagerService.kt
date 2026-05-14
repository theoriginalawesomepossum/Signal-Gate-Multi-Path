package com.signalgate.multipoint.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

/**
 * OverlayManagerService — PROTOTYPE (Bare Metal)
 *
 * GOAL: Prove the infrastructure works. Nothing more.
 *   ✓ Permission check works
 *   ✓ Service starts/stops cleanly
 *   ✓ Overlay attaches to WindowManager on RINGING
 *   ✓ Overlay detaches on IDLE
 *   ✓ No crashes
 *   ✓ No stuck overlays
 *
 * NO Compose. NO animations. NO UI polish.
 * A plain TextView in a colored box is the entire UI.
 *
 * When prototype passes all 5 checks above → replace this
 * file with the premium ShieldOverlayView version.
 */
class OverlayManagerService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    companion object {
        private const val TAG = "OverlayProto"

        const val ACTION_SHOW   = "com.signalgate.overlay.SHOW"
        const val ACTION_UPDATE = "com.signalgate.overlay.UPDATE"
        const val ACTION_HIDE   = "com.signalgate.overlay.HIDE"

        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_DECISION     = "extra_decision"
        const val EXTRA_REASON       = "extra_reason"

        // ── Permission check ────────────────────────────────────────────────
        fun checkOverlayPermission(context: Context): Boolean {
            return Settings.canDrawOverlays(context)
        }

        fun openPermissionSettings(context: Context) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    // ─── Service lifecycle ───────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "OverlayManagerService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        when (intent?.action) {
            ACTION_SHOW -> {
                val number   = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: "Unknown"
                val decision = intent.getStringExtra(EXTRA_DECISION)     ?: "SCREENING"
                showShield(number, decision)
            }
            ACTION_UPDATE -> {
                val decision = intent.getStringExtra(EXTRA_DECISION) ?: "SCREENING"
                updateShield(decision)
            }
            ACTION_HIDE -> {
                hideShield()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        // Safety net: if the OS kills the service, make sure the overlay is gone
        hideShield()
        Log.d(TAG, "OverlayManagerService destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─── Core overlay operations ─────────────────────────────────────────────

    private fun showShield(phoneNumber: String, decision: String) {
        // Guard 1: permission
        if (!checkOverlayPermission(this)) {
            Log.e(TAG, "FAIL — SYSTEM_ALERT_WINDOW permission not granted")
            return
        }
        // Guard 2: already showing
        if (overlayView != null) {
            Log.d(TAG, "Shield already visible — updating instead")
            updateShield(decision)
            return
        }

        // ── Build the simplest possible overlay view ─────────────────────
        // Just a TextView. Background color = the only "UI" for now.
        val label = TextView(this).apply {
            text      = "⚡ SignalGate\n$phoneNumber\n[$decision]"
            textSize  = 14f
            setTextColor(Color.WHITE)
            setPadding(32, 24, 32, 24)
            setBackgroundColor(decisionColor(decision))
        }

        val params = buildLayoutParams()

        try {
            windowManager?.addView(label, params)
            overlayView = label
            Log.d(TAG, "✓ Overlay attached — phone: $phoneNumber  decision: $decision")
        } catch (e: Exception) {
            // Catching broadly here intentionally — prototype needs to surface
            // every possible failure mode clearly in Logcat
            Log.e(TAG, "✗ Failed to attach overlay view: ${e.message}", e)
            overlayView = null
        }
    }

    private fun updateShield(decision: String) {
        val view = overlayView as? TextView
        if (view == null) {
            Log.w(TAG, "updateShield() — no overlay present, ignoring")
            return
        }
        // Just swap background color and text. No animation.
        view.setBackgroundColor(decisionColor(decision))
        view.text = "⚡ SignalGate\n[$decision]"
        Log.d(TAG, "✓ Overlay updated — decision: $decision")
    }

    private fun hideShield() {
        val view = overlayView ?: run {
            Log.d(TAG, "hideShield() called but no overlay present — no-op")
            return
        }
        try {
            windowManager?.removeView(view)
            Log.d(TAG, "✓ Overlay removed cleanly")
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error removing overlay: ${e.message}", e)
        } finally {
            overlayView = null
            stopSelf()
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            // These two flags are non-negotiable:
            // NOT_TOUCH_MODAL  → touches pass through to the dialer underneath
            // NOT_FOCUSABLE    → overlay never hijacks keyboard / back button
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 120
        }
    }

    /** Color-coded by decision state — the only visual feedback in the prototype */
    private fun decisionColor(decision: String): Int = when (decision) {
        "ALLOW"     -> Color.parseColor("#CC00C853") // semi-transparent green
        "BLOCK"     -> Color.parseColor("#CCD50000") // semi-transparent red
        "SCREENING" -> Color.parseColor("#CC1565C0") // semi-transparent blue
        else        -> Color.parseColor("#CC212121") // dark grey fallback
    }
}
