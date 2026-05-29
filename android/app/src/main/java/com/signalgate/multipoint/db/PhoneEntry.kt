package com.signalgate.multipoint.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "phone_entries",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["phoneNumber", "action"]),
        Index(value = ["sourceId"]),
        Index(value = ["accountId"]),
        Index(value = ["addedAt"])
    ]
)
data class PhoneEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val phoneNumber: String,           // Normalized E.164 format

    val action: ActionType,

    val sourceId: Long? = null,

    val isPattern: Boolean = false,

    val confidence: Int = 0,

    val addedAt: Long = Instant.now().toEpochMilli(),

    val metadata: String? = null,      // JSON for audit (source name, reason, etc.)

    /** For future multi-account support */
    val accountId: String = "default"
) {
    enum class ActionType {
        ALLOW, BLOCK
    }
}
