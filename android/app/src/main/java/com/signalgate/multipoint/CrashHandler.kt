package com.signalgate.multipoint

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val fileName = "crash_$timestamp.txt"

            val crashDir = File(context.filesDir, "crashes")
            crashDir.mkdirs()

            File(crashDir, fileName).writeText(
                buildString {
                    appendLine("=== CRASH REPORT ===")
                    appendLine("Time: $timestamp")
                    appendLine("Thread: ${thread.name}")
                    appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                    appendLine("Android: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                    appendLine("App version: ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}")
                    appendLine()
                    appendLine("=== STACK TRACE ===")
                    appendLine(stackTrace)
                }
            )
        } catch (e: Exception) {
            // Don't let the crash handler itself crash
        }

        // Let the system handle it normally (shows crash dialog)
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
