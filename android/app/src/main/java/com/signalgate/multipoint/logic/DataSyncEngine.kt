package com.signalgate.multipoint.logic

import android.util.Log
import com.signalgate.multipoint.database.SignalGateDatabase
import com.signalgate.multipoint.database.entities.SourceEntity
import com.signalgate.multipoint.database.entities.SyncHistoryEntry
import com.signalgate.multipoint.database.entities.UnifiedEntryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL

/**
 * DataSyncEngine handles syncing data from various sources (local files, remote URLs).
 * It implements chunked processing to handle large datasets efficiently.
 */
class DataSyncEngine(private val database: SignalGateDatabase) {

    companion object {
        private const val TAG = "DataSyncEngine"
        private const val CHUNK_SIZE = 1000 // Process entries in chunks of 1000
    }

    /**
     * Syncs a single data source.
     */
    suspend fun syncSource(sourceId: Int): SyncHistoryEntry {
        return withContext(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val source = database.sourceDao().getSourceById(sourceId)
                ?: return@withContext createFailedSyncHistory(sourceId, "Source not found", startTime)

            try {
                Log.d(TAG, "Starting sync for source: ${source.name}")

                val entries = when (source.type) {
                    "CSV" -> parseCSVFile(source.pathOrUrl)
                    "XLSX" -> parseXLSXFile(source.pathOrUrl)
                    "URL" -> fetchAndParseRemoteURL(source.pathOrUrl)
                    else -> {
                        Log.e(TAG, "Unknown source type: ${source.type}")
                        return@withContext createFailedSyncHistory(sourceId, "Unknown source type", startTime)
                    }
                }

                // Clear old entries for this source
                database.unifiedEntryDao().deleteEntriesBySourceId(sourceId)

                // Insert new entries in chunks
                var entriesAdded = 0
                for (i in entries.indices step CHUNK_SIZE) {
                    val chunk = entries.subList(
                        i,
                        minOf(i + CHUNK_SIZE, entries.size)
                    )
                    database.unifiedEntryDao().insertEntries(chunk)
                    entriesAdded += chunk.size
                }

                // Update source sync status
                val duration = System.currentTimeMillis() - startTime
                database.sourceDao().updateSourceSyncStatus(
                    sourceId,
                    System.currentTimeMillis(),
                    entriesAdded,
                    "HEALTHY"
                )

                Log.d(TAG, "Sync completed for source: ${source.name} ($entriesAdded entries)")

                return@withContext SyncHistoryEntry(
                    sourceId = sourceId,
                    status = "SUCCESS",
                    entriesAdded = entriesAdded,
                    duration = duration
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing source: ${source.name}", e)
                database.sourceDao().updateSourceSyncStatus(
                    sourceId,
                    System.currentTimeMillis(),
                    0,
                    "ERROR"
                )
                return@withContext createFailedSyncHistory(sourceId, e.message ?: "Unknown error", startTime)
            }
        }
    }

    /**
     * Syncs all enabled sources.
     */
    suspend fun syncAllSources(): List<SyncHistoryEntry> {
        return withContext(Dispatchers.IO) {
            val sources = database.sourceDao().getEnabledSources()
            val syncResults = mutableListOf<SyncHistoryEntry>()

            // Collect sources from Flow
            val sourceList = mutableListOf<SourceEntity>()
            sources.collect { sourceList.addAll(it) }

            for (source in sourceList) {
                val result = syncSource(source.id)
                syncResults.add(result)
                database.syncHistoryDao().insertSyncHistory(result)
            }

            return@withContext syncResults
        }
    }

    /**
     * Parses a CSV file and returns a list of UnifiedEntryEntity objects.
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
                    if (lineNumber == 1) return@forEachLine // Skip header

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
                                        sourceId = 0, // Will be set by caller
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
            return@withContext entries
        }
    }

    /**
     * Parses an XLSX file and returns a list of UnifiedEntryEntity objects.
     * Note: This is a placeholder. Full XLSX parsing requires Apache POI library.
     */
    private suspend fun parseXLSXFile(filePath: String): List<UnifiedEntryEntity> {
        return withContext(Dispatchers.IO) {
            // TODO: Implement XLSX parsing using Apache POI
            // For now, return empty list
            Log.w(TAG, "XLSX parsing not yet implemented")
            return@withContext emptyList()
        }
    }

    /**
     * Fetches data from a remote URL and parses it.
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
                        if (lineNumber == 1) return@forEachLine // Skip header

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
                                            sourceId = 0, // Will be set by caller
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

                Log.d(TAG, "Fetched and parsed ${entries.size} entries from URL")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching from URL: $urlString", e)
                throw e
            }

            return@withContext entries
        }
    }

    /**
     * Creates a failed sync history entry.
     */
    private fun createFailedSyncHistory(sourceId: Int, errorMessage: String, startTime: Long): SyncHistoryEntry {
        return SyncHistoryEntry(
            sourceId = sourceId,
            status = "FAILURE",
            errorMessage = errorMessage,
            duration = System.currentTimeMillis() - startTime
        )
    }
}
