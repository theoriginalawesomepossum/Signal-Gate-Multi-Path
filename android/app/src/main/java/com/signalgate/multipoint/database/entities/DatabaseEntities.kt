package com.signalgate.multipoint.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * SourceEntity represents a data source (local file or remote URL) in the MultiPoint Hub.
 */
@Entity(
    tableName = "sources",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["type"])
    ]
)
data class SourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String, // "CSV", "XLSX", "URL", "MANUAL"
    val pathOrUrl: String,
    val isEnabled: Boolean = true,
    val lastSynced: Long = 0, // Timestamp in milliseconds
    val priority: Int = 0, // 0 = lowest, higher = higher priority
    val entriesCount: Int = 0,
    val healthStatus: String = "UNKNOWN", // "HEALTHY", "ERROR", "DISABLED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * UnifiedEntryEntity represents a phone number entry with its action and source.
 * This unified table replaces separate block/allow tables for better performance.
 */
@Entity(
    tableName = "unified_entries",
    foreignKeys = [
        ForeignKey(
            entity = SourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["phoneNumber", "action"]),
        Index(value = ["sourceId"]),
        Index(value = ["isPattern"]),
        Index(value = ["action"])
    ]
)
data class UnifiedEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phoneNumber: String, // Normalized phone number or pattern
    val action: String, // "BLOCK", "ALLOW"
    val sourceId: Int, // Foreign key to SourceEntity
    val isPattern: Boolean = false, // True if phoneNumber is a pattern (e.g., +1800*)
    val category: String? = null, // e.g., "Telemarketing", "Robocall", "Scam"
    val confidence: Int? = null, // 0-100
    val riskLevel: String? = null, // "HIGH", "MEDIUM", "LOW"
    val metadata: String? = null, // JSON string for additional data
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * CallLogEntry represents a logged incoming call.
 */
@Entity(
    tableName = "call_log",
    indices = [
        Index(value = ["phoneNumber"]),
        Index(value = ["timestamp"]),
        Index(value = ["decision"])
    ]
)
data class CallLogEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val phoneNumber: String,
    val normalizedPhoneNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val decision: String, // "ALLOW", "BLOCK", "SCREEN"
    val spamStatus: String, // "LIKELY SPAM", "SPAM", "UNKNOWN", "SAFE"
    val spamCategory: String? = null,
    val confidence: Int? = null,
    val riskLevel: String? = null,
    val matchedSources: String? = null, // JSON array of source names
    val duration: Int = 0, // Duration in seconds
    val notes: String? = null
)

/**
 * SettingEntry represents application settings.
 */
@Entity(
    tableName = "settings",
    indices = [
        Index(value = ["key"], unique = true)
    ]
)
data class SettingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val key: String, // Setting key
    val value: String, // Setting value
    val type: String = "STRING", // "STRING", "INT", "BOOLEAN", "JSON"
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * SyncHistoryEntry tracks the sync history for each source.
 */
@Entity(
    tableName = "sync_history",
    foreignKeys = [
        ForeignKey(
            entity = SourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sourceId"]),
        Index(value = ["timestamp"])
    ]
)
data class SyncHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sourceId: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "SUCCESS", "FAILURE", "PARTIAL"
    val entriesAdded: Int = 0,
    val entriesUpdated: Int = 0,
    val entriesRemoved: Int = 0,
    val errorMessage: String? = null,
    val duration: Long = 0 // Duration in milliseconds
)
