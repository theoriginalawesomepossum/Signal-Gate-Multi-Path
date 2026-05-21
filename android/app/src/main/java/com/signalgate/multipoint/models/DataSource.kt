package com.signalgate.multipoint.models

/**
 * DataSource represents a single data source (local file or remote URL) in the MultiPoint Hub.
 */
data class DataSource(
    val id: Int,
    val name: String,
    val type: String, // "Local CSV", "Local XLSX", "Remote URL", "Manual Entry"
    val entriesCount: Int,
    val healthStatus: String, // "Healthy", "Error", "Disabled"
    val lastSynced: String,
    val isEnabled: Boolean
)
