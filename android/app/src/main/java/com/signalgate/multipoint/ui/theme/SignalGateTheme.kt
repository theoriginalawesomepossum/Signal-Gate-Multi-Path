package com.signalgate.multipoint.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00BCD4),
    secondary = Color(0xFFFFA726),
    tertiary = Color(0xFF4CAF50),
    background = Color(0xFF1a1a1a),
    surface = Color(0xFF2a2a2a),
    error = Color(0xFFFF5252)
)

@Composable
fun SignalGateTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
