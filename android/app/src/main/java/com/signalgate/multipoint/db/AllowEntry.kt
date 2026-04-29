package com.signalgate.multipoint.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Entity(tableName = "allow_entries")
data class AllowEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val label: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface AllowDao {
    @Query("SELECT * FROM allow_entries")
    suspend fun getAll(): List<AllowEntry>

    @Query("SELECT * FROM allow_entries WHERE phoneNumber = :number LIMIT 1")
    suspend fun findByNumber(number: String): AllowEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AllowEntry)

    @Query("DELETE FROM allow_entries WHERE phoneNumber = :number")
    suspend fun deleteByNumber(number: String)
}
