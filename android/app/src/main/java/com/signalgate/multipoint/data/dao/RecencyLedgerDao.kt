package com.signalgate.multipoint.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

data class ActionLedgerEntity(
    val id: Long = 0,
    val targetNumber: String,
    val previousActionType: String,  // e.g. "BLOCKED", "ALLOWED"
    val timestamp: Long
)

@Dao
interface RecencyLedgerDao {

    @Insert
    suspend fun logAction(action: ActionLedgerEntity)

    @Query("SELECT * FROM action_ledger ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastAction(): ActionLedgerEntity?

    @Query("DELETE FROM action_ledger WHERE id = :id")
    suspend fun removeLedgerEntry(id: Long)

    @Query("DELETE FROM call_logs WHERE number = :number")
    suspend fun deleteRuleByNumber(number: String)

    /**
     * Executes an Atomic Transaction Rollback to support the single-tap "Undo" feature safely in the UI.
     */
    @Transaction
    suspend fun executeUndoAction() {
        val lastAction = getLastAction() ?: return
        // Remove the accidental rule enforcement
        deleteRuleByNumber(lastAction.targetNumber)
        
        // Purge the action from the history log transaction record cleanly
        removeLedgerEntry(lastAction.id)
    }
}
