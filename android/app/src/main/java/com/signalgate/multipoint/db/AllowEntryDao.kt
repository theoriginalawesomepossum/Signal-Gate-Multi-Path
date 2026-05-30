package com.signalgate.multipoint.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AllowEntryDao {
    @Insert
    suspend fun insert(allowEntry: AllowEntry)

    @Query("SELECT * FROM allow_entries")
    suspend fun getAll(): List<AllowEntry>
}
