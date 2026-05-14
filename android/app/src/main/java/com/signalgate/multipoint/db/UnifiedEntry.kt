package com.signalgate.multipoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Index

@Entity(
    tableName = "unified_entries",
    indices = [Index(value = ["phoneNumber", "action"])]
)
data class UnifiedEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val action: String, // "BLOCK", "ALLOW"
    val sourceId: Int, // References Source.id, 0 for Manual
    val isPattern: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface UnifiedEntryDao {
    @Query("SELECT * FROM unified_entries WHERE phoneNumber = :number")
    suspend fun findByNumber(number: String): List<UnifiedEntry>

    @Query("SELECT * FROM unified_entries WHERE isPattern = 1")
    suspend fun getAllPatterns(): List<UnifiedEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: UnifiedEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<UnifiedEntry>)

    @Query("DELETE FROM unified_entries WHERE phoneNumber = :number AND sourceId = :sourceId")
    suspend fun delete(number: String, sourceId: Int)

    @Query("DELETE FROM unified_entries WHERE sourceId = :sourceId")
    suspend fun deleteBySource(sourceId: Int)
}
