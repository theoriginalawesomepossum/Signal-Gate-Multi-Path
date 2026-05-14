package com.signalgate.multipoint

import android.app.Application
import android.os.Process
import android.content.Intent
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.signalgate.multipoint.db.AppDatabase
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    // Background scope for initialization work
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(CrashHandler(this))

        // Initialize heavy components off the main thread
        applicationScope.launch {
            try {
                // Pre-warm the database (creates DB + runs migrations if any)
                AppDatabase.getDatabase(this@MainApplication)
                
                Log.d("MainApplication", "Database pre-warmed successfully")
                
                scheduleSync()

            } catch (e: Exception) {
                Log.e("MainApplication", "Error during background initialization", e)
            }
        }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<MultiPointSyncWorker>(
            12, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MultiPointSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
