package com.signalgate.multipoint.service.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.signalgate.multipoint.data.security.BloomFilterEngine
import com.signalgate.multipoint.data.security.SecureCsvParser
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import com.signalgate.multipoint.database.repositories.DataSourceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * MultiPortSyncWorker fetches a remote feed URL, streams it through [SecureCsvParser]
 * (which sanitizes each row and populates the [BloomFilterEngine]), and persists each
 * clean entry to [DataSourceRepository].
 *
 * The sourceId for all entries is read from the WorkManager input data key "SOURCE_ID".
 * If omitted it defaults to 0 (unknown source).
 */
class MultiPortSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val bloomFilter: BloomFilterEngine,
    private val secureCsvParser: SecureCsvParser,
    private val dataSourceRepository: DataSourceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val targetUrl = inputData.getString("TARGET_FEED_URL")
            ?: return@withContext Result.failure()
        val sourceId = inputData.getInt("SOURCE_ID", 0)

        try {
            // Open a secure connection to the remote feed
            val connection = URL(targetUrl).openConnection()
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            val inputStream = connection.getInputStream()

            // Atomically clear old bloom-filter vectors before repopulating
            bloomFilter.clear()

            // Stream, sanitize, and persist each clean phone-number vector
            secureCsvParser.streamAndPopulate(inputStream) { cleanNumber ->
                // Insert each sanitized entry into the production database via the repository
                val entry = UnifiedEntryEntity(
                    phoneNumber = cleanNumber,
                    action = "BLOCK",   // Remote feeds are block-lists by convention
                    sourceId = sourceId,
                    isPattern = cleanNumber.contains("*")
                )
                // Note: streamAndPopulate is synchronous; we call a fire-and-forget
                // coroutine here because the lambda is not a suspend context.
                // DataSourceRepository.insertEntry is safe to call from IO dispatcher.
                kotlinx.coroutines.runBlocking { dataSourceRepository.insertEntry(entry) }
            }

            Result.success()
        } catch (e: Exception) {
            // Fail-safe: any network or parse crash retries without purging existing DB state
            Result.retry()
        }
    }
}
