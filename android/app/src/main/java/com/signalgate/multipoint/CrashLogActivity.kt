package com.signalgate.multipoint

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class CrashLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set theme programmatically or ensure it uses the app theme
        setTheme(R.style.app_theme)
        
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(android.graphics.Color.parseColor("#121212")) // background_primary
            setPadding(32, 32, 32, 32)
        }

        val header = TextView(this).apply {
            text = "SYSTEM CRASH LOGS"
            textSize = 18f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(0, 0, 0, 32)
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        layout.addView(header)

        val shareButton = com.google.android.material.button.MaterialButton(this).apply {
            text = "SHARE LOG REPORT"
            setBackgroundColor(android.graphics.Color.parseColor("#4285F4")) // button_primary
            setTextColor(android.graphics.Color.WHITE)
        }
        layout.addView(shareButton)

        val scrollView = ScrollView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            setPadding(0, 32, 0, 0)
        }
        
        val textView = TextView(this).apply {
            textSize = 12f
            setTextColor(android.graphics.Color.parseColor("#BDBDBD")) // text_secondary
            typeface = android.graphics.Typeface.MONOSPACE
        }
        scrollView.addView(textView)
        layout.addView(scrollView)

        setContentView(layout)

        val crashDir = File(filesDir, "crashes")
        val logs = crashDir.listFiles()
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()

        if (logs.isEmpty()) {
            textView.text = "No crash logs found."
        } else {
            val content = logs.joinToString("\n\n${"=".repeat(60)}\n\n") { 
                "FILE: ${it.name}\n\n${it.readText()}" 
            }
            textView.text = content

            shareButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content)
                    putExtra(Intent.EXTRA_SUBJECT, "Crash Logs")
                }
                startActivity(Intent.createChooser(intent, "Share crash logs"))
            }
        }
    }
}
