package com.signalgate.multipoint

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signalgate.multipoint.db.AppDatabase

class MultiPointSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val sourceDao = db.sourceDao()
        val syncEngine = SyncEngine(applicationContext)
        val benchmarkEngine = BenchmarkEngine(applicationContext)

        return try {
            val enabledSources = sourceDao.getEnabledSources()
            val rowLimit = benchmarkEngine.getRowLimit()

            // Calculate limit per source (simple division for now)
            val limitPerSource = if (enabledSources.isNotEmpty()) {
                rowLimit / enabledSources.size
            } else {
                rowLimit
            }

            for (source in enabledSources) {
                syncEngine.syncSource(source, limitPerSource)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
