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

        val scrollView = ScrollView(this)
        val textView = TextView(this).apply {
            textSize = 10f
            fontFeatureSettings = "monospace"
            setPadding(16, 16, 16, 16)
        }
        scrollView.addView(textView)

        val shareButton = Button(this).apply {
            text = "Share Logs"
        }

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(shareButton)
            addView(scrollView)
        }
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
