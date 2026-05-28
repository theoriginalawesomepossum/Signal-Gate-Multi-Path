package com.signalgate.multipoint.models

/**
 * PermissionItem represents an Android permission and its current status in the UI.
 */
data class PermissionItem(
    val id: Int,
    val name: String,
    val description: String,
    val androidPermission: String?, // null for special settings like Overlay
    var isGranted: Boolean,
    val isSpecialSetting: Boolean = false
)
