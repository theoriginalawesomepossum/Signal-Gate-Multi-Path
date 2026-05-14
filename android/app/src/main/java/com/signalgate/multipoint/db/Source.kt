package com.signalgate.multipoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "sources")
data class Source(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "LOCAL_FILE", "REMOTE_URL", "MANUAL"
    val pathOrUrl: String,
    val isEnabled: Boolean = true,
    val lastSynced: Long = 0,
    val priority: Int = 100 // Lower number = Higher priority
)

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY priority ASC")
    suspend fun getAll(): List<Source>

    @Query("SELECT * FROM sources WHERE isEnabled = 1 ORDER BY priority ASC")
    suspend fun getEnabledSources(): List<Source>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(source: Source): Long

    @Update
    suspend fun update(source: Source)

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun deleteById(id: Int)
}
