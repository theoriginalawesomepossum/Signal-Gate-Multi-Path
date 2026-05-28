package com.signalgate.multipoint.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Unified entity replacing separate BlockEntry/AllowEntry.
 * Supports Manual, Pattern, and Aggregated sources with priority logic.
 */
@Entity(
    tableName = "phone_entries",
    indices = [
        Index(value = ["phoneNumber"], unique = false),
        Index(value = ["phoneNumber", "action"]),
        Index(value = ["sourceId"]),
        Index(value = ["addedAt"])
    ]
)
data class PhoneEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Normalized E.164 format, e.g. "+18005550199" */
    val phoneNumber: String,

    val action: ActionType,

    /** Null for manual/pattern entries, otherwise links to DataSource */
    val sourceId: Long? = null,

    /** True for prefix/pattern rules like "+1800" */
    val isPattern: Boolean = false,

    /** Confidence score from community sources (0-100) */
    val confidence: Int = 0,

    val addedAt: Long = Instant.now().toEpochMilli(),

    /** JSON string with source name, reason, etc. for audit */
    val metadata: String? = null
) {
    enum class ActionType {
        ALLOW, BLOCK
    }
}
