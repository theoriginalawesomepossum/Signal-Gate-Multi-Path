package com.signalgate.multipoint.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BlockEntryDao {
    @Insert
    suspend fun insert(blockEntry: BlockEntry)

    @Query("SELECT * FROM block_entries")
    suspend fun getAll(): List<BlockEntry>
}
