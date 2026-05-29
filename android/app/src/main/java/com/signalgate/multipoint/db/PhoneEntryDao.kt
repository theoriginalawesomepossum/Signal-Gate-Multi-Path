package com.signalgate.multipoint.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PhoneEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PhoneEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PhoneEntry>)

    @Transaction
    @Query("SELECT * FROM phone_entries WHERE phoneNumber = :number AND action = :action LIMIT 1")
    suspend fun findByNumberAndAction(number: String, action: PhoneEntry.ActionType): PhoneEntry?

    @Transaction
    @Query("""
        SELECT * FROM phone_entries 
        WHERE isPattern = 1 
        AND :number LIKE phoneNumber || '%' 
        ORDER BY LENGTH(phoneNumber) DESC 
        LIMIT 1
    """)
    suspend fun findMatchingPattern(number: String): PhoneEntry?

    @Transaction
    @Query("""
        SELECT * FROM phone_entries 
        WHERE phoneNumber = :number 
        AND sourceId IS NOT NULL 
        AND action = 'BLOCK'
        LIMIT 1
    """)
    suspend fun findInEnabledSources(number: String): PhoneEntry?

    @Query("DELETE FROM phone_entries WHERE phoneNumber = :number")
    suspend fun deleteByNumber(number: String)

    @Query("SELECT COUNT(*) FROM phone_entries WHERE accountId = :accountId")
    suspend fun getCountByAccount(accountId: String = "default"): Int
}
