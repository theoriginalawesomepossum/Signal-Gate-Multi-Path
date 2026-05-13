package com.signalgate.multipoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Entity(tableName = "call_log")
data class CallLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val decision: String, // e.g., "BLOCKED", "ALLOWED"
    val reason: String? = null // e.g., "Exact match in blocklist", "Pattern match"
)

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_log ORDER BY timestamp DESC")
    suspend fun getAll(): List<CallLogEntry>

    @Insert
    suspend fun insert(entry: CallLogEntry)

    @Query("DELETE FROM call_log")
    suspend fun clearLog()
}
