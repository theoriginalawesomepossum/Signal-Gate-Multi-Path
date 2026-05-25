package com.signalgate.multipoint.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Color definitions based on colors.xml
val BackgroundPrimary = Color(0xFF0A0E14)
val BackgroundSecondary = Color(0xFF121A26)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFA0AEC0)
val TextMuted = Color(0xFF718096)
val AccentPrimary = Color(0xFF00A3FF)
val AccentSecondary = Color(0xFF00D1FF)
val StatusHigh = Color(0xFFFF4D4D)
val StatusMedium = Color(0xFFFFA500)
val StatusLow = Color(0xFF00C853)
val OverlayGlass = Color(0xCC121A26)

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    tertiary = StatusLow,
    background = BackgroundPrimary,
    surface = BackgroundSecondary,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = StatusHigh
)

private val LightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    secondary = AccentSecondary,
    tertiary = StatusLow,
    background = Color.White,
    surface = Color(0xFFF7FAFC),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A202C),
    onSurface = Color(0xFF1A202C),
    error = StatusHigh
)

val SignalGateTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun SignalGateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SignalGateTypography,
        content = content
    )
}
