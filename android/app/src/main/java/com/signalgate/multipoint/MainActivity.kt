package com.signalgate.multipoint

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // temporary simple UI (prevents blank crash)
        val view = android.widget.TextView(this)
        view.text = "Signal Gate Multi-Path"
        view.textSize = 20f
        view.gravity = android.view.Gravity.CENTER

        setContentView(view)
    }
}
