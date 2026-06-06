package com.signalgate.multipoint.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Sources : Screen("sources", "Sources", Icons.Default.List)
    object CallLog : Screen("call_log", "Call Log", Icons.Default.Phone)
    object BlockAllowList : Screen("block_list", "Block / Allow Lists", Icons.Default.Lock)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}
