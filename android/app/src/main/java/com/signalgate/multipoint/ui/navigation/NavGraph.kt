package com.signalgate.multipoint.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.signalgate.multipoint.ui.screens.OperationalDashboard
import com.signalgate.multipoint.ui.screens.CallLogScreen
import com.signalgate.multipoint.ui.screens.SettingsScreen
import com.signalgate.multipoint.ui.screens.SourcesScreen

@Composable
fun SignalGateNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onOpenDrawer: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Screen.Dashboard.route) {
            OperationalDashboard(
                onOpenDrawer = onOpenDrawer
            )
        }
        composable(Screen.Sources.route) {
            SourcesScreen()
        }
        composable(Screen.CallLog.route) {
            CallLogScreen()
        }
        composable(Screen.BlockAllowList.route) {
            Text(
                text = "Block / Allow List — Coming Soon",
                color = androidx.compose.ui.graphics.Color.White
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
