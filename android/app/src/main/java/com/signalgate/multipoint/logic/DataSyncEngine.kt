package com.signalgate.multipoint.logic

import android.util.Log
import com.signalgate.multipoint.database.entities.SyncHistoryEntry
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import com.signalgate.multipoint.database.repositories.DataSourceRepository
import com.signalgate.multipoint.database.repositories.SyncHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL

/**
 * DataSyncEngine handles syncing data from various sources (local files, remote URLs).
 * Uses [DataSourceRepository] and [SyncHistoryRepository] as the production data path —
 * no direct database or DAO access.
 *
 * Implements chunked processing to handle large datasets efficiently.
 */
class DataSyncEngine(
    private val dataSourceRepository: DataSourceRepository,
    private val syncHistoryRepository: SyncHistoryRepository
) {

    companion object {
        private const val TAG = "DataSyncEngine"
        private const val CHUNK_SIZE = 1000 // Process entries in chunks of 1000
    }

    /**
     * Syncs a single data source by [sourceId].
     * Returns a [SyncHistoryEntry] describing the outcome.
     */
    suspend fun syncSource(sourceId: Int): SyncHistoryEntry {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val source = dataSourceRepository.getSourceById(sourceId)
                ?: return@withContext createFailedSyncHistory(sourceId, "Source not found", startTime)

            try {
                Log.d(TAG, "Starting sync for source: ${source.name}")

                val rawEntries = when (source.type) {
                    "CSV" -> parseCSVFile(source.pathOrUrl)
                    "XLSX" -> parseXLSXFile(source.pathOrUrl)
                    "URL" -> fetchAndParseRemoteURL(source.pathOrUrl)
                    else -> {
                        Log.e(TAG, "Unknown source type: ${source.type}")
                        return@withContext createFailedSyncHistory(
                            sourceId, "Unknown source type: ${source.type}", startTime
                        )
                    }
                }

                // Stamp each entry with the correct sourceId before inserting
                val entries = rawEntries.map { it.copy(sourceId = sourceId) }

                // Insert new entries in chunks via the repository
                var entriesAdded = 0
                for (i in entries.indices step CHUNK_SIZE) {
                    val chunk = entries.subList(i, minOf(i + CHUNK_SIZE, entries.size))
                    chunk.forEach { dataSourceRepository.insertEntry(it) }
                    entriesAdded += chunk.size
                }

                // Update source sync status via the repository
                val duration = System.currentTimeMillis() - startTime
                dataSourceRepository.updateSourceSyncStatus(
                    sourceId = sourceId,
                    timestamp = System.currentTimeMillis(),
                    entriesCount = entriesAdded,
                    healthStatus = "HEALTHY"
                )

                Log.d(TAG, "Sync completed for source: ${source.name} ($entriesAdded entries)")

                SyncHistoryEntry(
                    sourceId = sourceId,
                    status = "SUCCESS",
                    entriesAdded = entriesAdded,
                    duration = duration
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing source: ${source.name}", e)
                dataSourceRepository.updateSourceSyncStatus(
                    sourceId = sourceId,
                    timestamp = System.currentTimeMillis(),
                    entriesCount = 0,
                    healthStatus = "ERROR"
                )
                createFailedSyncHistory(sourceId, e.message ?: "Unknown error", startTime)
            }
        }
    }

    /**
     * Syncs all enabled sources and persists each result to [SyncHistoryRepository].
     */
    suspend fun syncAllSources(): List<SyncHistoryEntry> {
        return withContext(Dispatchers.IO) {
            val enabledSources = dataSourceRepository.getEnabledSources().first()
            val syncResults = mutableListOf<SyncHistoryEntry>()

            for (source in enabledSources) {
                val result = syncSource(source.id)
                syncHistoryRepository.insertSyncHistory(result)
                syncResults.add(result)
            }

            syncResults
        }
    }

    // ── Parsers ───────────────────────────────────────────────────────────────

    /**
     * Parses a local CSV file and returns a list of [UnifiedEntryEntity] objects.
     * sourceId is set to 0 here and stamped by the caller before insertion.
     */
    private suspend fun parseCSVFile(filePath: String): List<UnifiedEntryEntity> {
        return withContext(Dispatchers.IO) {
            val entries = mutableListOf<UnifiedEntryEntity>()
            val file = File(filePath)

            if (!file.exists()) {
                throw IllegalArgumentException("File not found: $filePath")
            }

            file.bufferedReader().use { reader ->
                var lineNumber = 0
                reader.forEachLine { line ->
                    lineNumber++
                    if (lineNumber == 1) return@forEachLine // Skip header row

                    val parts = line.split(",")
                    if (parts.size >= 2) {
                        try {
                            val phoneNumber = parts[0].trim()
                            val action = parts[1].trim().uppercase()

                            if (phoneNumber.isNotEmpty() && (action == "BLOCK" || action == "ALLOW")) {
                                entries.add(
                                    UnifiedEntryEntity(
                                        phoneNumber = phoneNumber,
                                        action = action,
                                        sourceId = 0, // Stamped by caller
                                        isPattern = phoneNumber.contains("*"),
                                        category = if (parts.size > 2) parts[2].trim() else null,
                                        confidence = if (parts.size > 3) parts[3].trim().toIntOrNull() else null
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing CSV line $lineNumber: $line", e)
                        }
                    }
                }
            }

            Log.d(TAG, "Parsed ${entries.size} entries from CSV file")
            entries
        }
    }

    /**
     * Placeholder for XLSX parsing. Full implementation requires Apache POI.
     */
    @Suppress("UNUSED_PARAMETER")
    private suspend fun parseXLSXFile(filePath: String): List<UnifiedEntryEntity> {
        return withContext(Dispatchers.IO) {
            // TODO: Implement XLSX parsing using Apache POI
            Log.w(TAG, "XLSX parsing not yet implemented")
            emptyList()
        }
    }

    /**
     * Fetches data from a remote URL and parses it as CSV.
     */
    private suspend fun fetchAndParseRemoteURL(urlString: String): List<UnifiedEntryEntity> {
        return withContext(Dispatchers.IO) {
            val entries = mutableListOf<UnifiedEntryEntity>()

            try {
                val url = URL(urlString)
                val connection = url.openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
                    var lineNumber = 0
                    reader.forEachLine { line ->
                        lineNumber++
                        if (lineNumber == 1) return@forEachLine // Skip header row

                        val parts = line.split(",")
                        if (parts.size >= 2) {
                            try {
                                val phoneNumber = parts[0].trim()
                                val action = parts[1].trim().uppercase()

                                if (phoneNumber.isNotEmpty() && (action == "BLOCK" || action == "ALLOW")) {
                                    entries.add(
                                        UnifiedEntryEntity(
                                            phoneNumber = phoneNumber,
                                            action = action,
                                            sourceId = 0, // Stamped by caller
                                            isPattern = phoneNumber.contains("*"),
                                            category = if (parts.size > 2) parts[2].trim() else null,
                                            confidence = if (parts.size > 3) parts[3].trim().toIntOrNull() else null
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing remote URL line $lineNumber: $line", e)
                            }
                        }
                    }
                }

                Log.d(TAG, "Fetched and parsed ${entries.size} entries from URL: $urlString")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from URL: $urlString", e)
                throw e
            }

            entries
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun createFailedSyncHistory(
        sourceId: Int,
        errorMessage: String,
        startTime: Long
    ): SyncHistoryEntry {
        return SyncHistoryEntry(
            sourceId = sourceId,
            status = "FAILURE",
            errorMessage = errorMessage,
            duration = System.currentTimeMillis() - startTime
        )
    }
}
