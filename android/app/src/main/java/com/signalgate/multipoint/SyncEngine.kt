package com.signalgate.multipoint

import android.content.Context
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.signalgate.multipoint.db.AppDatabase
import com.signalgate.multipoint.db.Source
import com.signalgate.multipoint.db.UnifiedEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class SyncEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val unifiedDao = db.unifiedEntryDao()
    private val sourceDao = db.sourceDao()

    suspend fun syncSource(source: Source, limit: Int = Int.MAX_VALUE) = withContext(Dispatchers.IO) {
        try {
            // 1. Clear existing entries for this source
            unifiedDao.deleteBySource(source.id)

            // 2. Fetch data based on type
            val inputStream = when (source.type) {
                "REMOTE_URL" -> URL(source.pathOrUrl).openStream()
                "LOCAL_FILE" -> File(source.pathOrUrl).inputStream()
                else -> return@withContext
            }

            // 3. Parse and Insert in chunks
            var count = 0
            val batchSize = 1000
            val currentBatch = mutableListOf<UnifiedEntry>()

            csvReader().open(inputStream) {
                readAllAsSequence().forEach { row ->
                    if (count >= limit) return@forEach
                    
                    val number = row.getOrNull(0) ?: return@forEach
                    val action = row.getOrNull(1)?.uppercase() ?: "BLOCK"
                    val isPattern = row.getOrNull(2)?.toBoolean() ?: false

                    currentBatch.add(
                        UnifiedEntry(
                            phoneNumber = number,
                            action = action,
                            sourceId = source.id,
                            isPattern = isPattern
                        )
                    )

                    if (currentBatch.size >= batchSize) {
                        unifiedDao.insertAll(currentBatch)
                        currentBatch.clear()
                    }
                    count++
                }
                // Insert remaining entries
                if (currentBatch.isNotEmpty()) {
                    unifiedDao.insertAll(currentBatch)
                }
            }

            // Update last synced time
            sourceDao.update(source.copy(lastSynced = System.currentTimeMillis()))

        } catch (e: Exception) {
            e.printStackTrace()
            // In Phase 4, we'll log this to the CrashLog system
        }
    }
}
