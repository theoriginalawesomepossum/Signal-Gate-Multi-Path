package com.signalgate.multipoint.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhoneEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PhoneEntry)

    @Query("SELECT * FROM phone_entries WHERE phoneNumber = :number AND action = :action LIMIT 1")
    suspend fun findByNumberAndAction(number: String, action: PhoneEntry.ActionType): PhoneEntry?

    @Query("SELECT * FROM phone_entries WHERE phoneNumber LIKE :pattern || '%' AND isPattern = 1 LIMIT 1")
    suspend fun findMatchingPattern(pattern: String): PhoneEntry?

    @Query("""
        SELECT * FROM phone_entries 
        WHERE phoneNumber = :number 
        AND sourceId IS NOT NULL 
        LIMIT 1
    """)
    suspend fun findInSources(number: String): PhoneEntry?
}
