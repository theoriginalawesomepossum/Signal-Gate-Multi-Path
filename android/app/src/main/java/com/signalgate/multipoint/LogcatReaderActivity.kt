package com.signalgate.multipoint

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * LogcatReaderActivity
 *
 * Mirrors CrashLogActivity exactly — same layout pattern, same style,
 * same Share button behavior.
 *
 * Instead of reading crash files from disk, it runs:
 *   logcat -d -t 200 -v time
 * filtered to the tags that matter for overlay prototype testing:
 *   OverlayProto, PhoneStateReceiver, OverlayManagerService
 *
 * The -d flag dumps the current buffer and exits (no streaming).
 * The -t 200 flag limits to the last 200 lines — keeps it readable.
 *
 * Launched from SettingsFragment via the "View Debug Logs" button.
 */
class LogcatReaderActivity : AppCompatActivity() {

    // Tags we care about for overlay prototype validation.
    // Add more here as Phase 3 grows (e.g. "SyncEngine", "CallScreening").
    private val filteredTags = listOf(
        "OverlayProto",
        "OverlayManagerService",
        "PhoneStateReceiver"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.app_theme)

        // ── Root layout — identical structure to CrashLogActivity ────────────
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(32, 32, 32, 32)
        }

        val header = TextView(this).apply {
            text = "OVERLAY DEBUG LOGS"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 16)
            typeface = Typeface.DEFAULT_BOLD
        }
        layout.addView(header)

        val subHeader = TextView(this).apply {
            text = "Tags: ${filteredTags.joinToString(" · ")}"
            textSize = 11f
            setTextColor(Color.parseColor("#BDBDBD"))
            setPadding(0, 0, 0, 32)
        }
        layout.addView(subHeader)

        val shareButton = MaterialButton(this).apply {
            text = "SHARE LOG REPORT"
            setBackgroundColor(Color.parseColor("#4285F4"))
            setTextColor(Color.WHITE)
        }
        layout.addView(shareButton)

        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            setPadding(0, 32, 0, 0)
        }

        val textView = TextView(this).apply {
            textSize = 11f
            setTextColor(Color.parseColor("#BDBDBD"))
            typeface = Typeface.MONOSPACE
        }
        scrollView.addView(textView)
        layout.addView(scrollView)

        setContentView(layout)

        // ── Read logcat ───────────────────────────────────────────────────────
        val logContent = readFilteredLogcat()

        textView.text = logContent

        // Scroll to bottom automatically — most recent logs are at the end
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }

        // ── Share button — identical behavior to CrashLogActivity ─────────────
        shareButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, logContent)
                putExtra(Intent.EXTRA_SUBJECT, "SignalGate Overlay Debug Logs")
            }
            startActivity(Intent.createChooser(intent, "Share debug logs"))
        }
    }

    /**
     * Runs logcat, captures the last 200 lines, then filters client-side
     * to only lines containing our target tags.
     *
     * Why client-side filter instead of logcat -s tag:level?
     * The -s flag requires exact level suffixes (e.g. OverlayProto:D).
     * Filtering after capture is more forgiving and catches all log levels
     * (D, I, W, E) without extra configuration.
     */
    private fun readFilteredLogcat(): String {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("logcat", "-d", "-t", "200", "-v", "time")
            )
            val output = process.inputStream
                .bufferedReader()
                .readLines()

            // Keep only lines that contain at least one of our tags
            val filtered = output.filter { line ->
                filteredTags.any { tag -> line.contains(tag) }
            }

            if (filtered.isEmpty()) {
                buildString {
                    appendLine("No overlay log entries found.")
                    appendLine()
                    appendLine("This means either:")
                    appendLine("  • No incoming calls have been received yet")
                    appendLine("  • The overlay service has not started")
                    appendLine("  • The SYSTEM_ALERT_WINDOW permission was not granted")
                    appendLine()
                    appendLine("Try making a test call, then reopen this screen.")
                }
            } else {
                buildString {
                    appendLine("=== Last ${filtered.size} matching log lines ===")
                    appendLine()
                    filtered.forEach { appendLine(it) }
                }
            }
        } catch (e: Exception) {
            // Logcat can fail if the process is killed or permissions are denied
            "Failed to read logcat: ${e.message}\n\nThis is unusual — try reopening the screen."
        }
    }
}
