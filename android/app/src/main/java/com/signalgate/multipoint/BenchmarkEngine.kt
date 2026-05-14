package com.signalgate.multipoint

import android.content.Context
import androidx.preference.PreferenceManager

class BenchmarkEngine(private val context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    enum class PerformanceLevel(val rowLimit: Int) {
        FPP(10_000),          // Fewest Points Possible
        CENTER_POINT(100_000), // Standard
        FULL_THROTTLE(500_000) // Maximum
    }

    fun getPerformanceLevel(): PerformanceLevel {
        val levelName = prefs.getString("performance_level", PerformanceLevel.CENTER_POINT.name)
        return try {
            PerformanceLevel.valueOf(levelName!!)
        } catch (e: Exception) {
            PerformanceLevel.CENTER_POINT
        }
    }

    fun setPerformanceLevel(level: PerformanceLevel) {
        prefs.edit().putString("performance_level", level.name).apply()
    }

    fun getRowLimit(): Int {
        return getPerformanceLevel().rowLimit
    }
}
