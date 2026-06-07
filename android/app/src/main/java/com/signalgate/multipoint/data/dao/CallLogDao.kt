package com.signalgate.multipoint.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    // Emits a new list automatically whenever an incoming call is screened or modified
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC LIMIT 100")
    fun getReactiveTelemetryLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(log: CallLogEntity)
}