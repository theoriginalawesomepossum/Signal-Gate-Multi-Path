package com.signalgate.multipoint.database.repositories

import com.signalgate.multipoint.database.daos.SourceDao
import com.signalgate.multipoint.database.daos.UnifiedEntryDao
import com.signalgate.multipoint.database.entities.SourceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataSourceRepository handles all data source operations and state management.
 * Acts as a single source of truth for data source data.
 */
class DataSourceRepository(
    private val sourceDao: SourceDao,
    private val entryDao: UnifiedEntryDao
) {

    /**
     * Gets all data sources from the database.
     */
    fun getAllSources(): Flow<List<SourceEntity>> {
        return sourceDao.getAllSources()
    }

    /**
     * Gets only enabled data sources.
     */
    fun getEnabledSources(): Flow<List<SourceEntity>> {
        return sourceDao.getEnabledSources()
    }

    /**
     * Gets the count of all sources.
     */
    fun getSourceCount(): Flow<Int> {
        return sourceDao.getSourceCount()
    }

    /**
     * Gets the count of enabled sources.
     */
    fun getEnabledSourceCount(): Flow<Int> {
        return getAllSources().map { sources ->
            sources.count { it.isEnabled }
        }
    }

    /**
     * Gets a specific source by ID.
     */
    suspend fun getSourceById(id: Int): SourceEntity? {
        return sourceDao.getSourceById(id)
    }

    /**
     * Inserts a new data source.
     */
    suspend fun insertSource(source: SourceEntity): Long {
        return sourceDao.insertSource(source)
    }

    /**
     * Updates an existing data source.
     */
    suspend fun updateSource(source: SourceEntity) {
        sourceDao.updateSource(source)
    }

    /**
     * Deletes a data source.
     */
    suspend fun deleteSource(source: SourceEntity) {
        sourceDao.deleteSource(source)
    }

    /**
     * Toggles the enabled state of a data source.
     * This will trigger the LED indicator update via the Flow.
     */
    suspend fun toggleSourceEnabled(sourceId: Int, isEnabled: Boolean) {
        sourceDao.updateSourceEnabled(sourceId, isEnabled)
    }

    /**
     * Updates the sync status of a data source.
     */
    suspend fun updateSourceSyncStatus(
        sourceId: Int,
        timestamp: Long,
        entriesCount: Int,
        healthStatus: String
    ) {
        sourceDao.updateSourceSyncStatus(sourceId, timestamp, entriesCount, healthStatus)
    }

    /**
     * Gets the entry count for a specific source.
     */
    suspend fun getEntryCountBySourceId(sourceId: Int): Int {
        return entryDao.getEntryCountBySourceId(sourceId)
    }

    /**
     * Gets the total entry count across all sources.
     */
    fun getTotalEntryCount(): Flow<Int> {
        return entryDao.getTotalEntryCount()
    }

    /**
     * Gets the total entry count for enabled sources only.
     */
    fun getEnabledSourcesEntryCount(): Flow<Int> {
        return getEnabledSources().map { sources ->
            var total = 0
            sources.forEach { source ->
                total += source.entriesCount
            }
            total
        }
    }
}
