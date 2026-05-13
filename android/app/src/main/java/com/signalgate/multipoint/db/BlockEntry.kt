package com.signalgate.multipoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "block_entries")
data class BlockEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val label: String? = null,
    val isPattern: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface BlockDao {
    @Query("SELECT * FROM block_entries")
    suspend fun getAll(): List<BlockEntry>

    @Query("SELECT * FROM block_entries WHERE phoneNumber = :number LIMIT 1")
    suspend fun findByNumber(number: String): BlockEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BlockEntry)

    @Query("DELETE FROM block_entries WHERE phoneNumber = :number")
    suspend fun deleteByNumber(number: String)
}
