package com.signalgate.multipoint.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.signalgate.multipoint.OperationalDashboard

@Composable
fun SignalGateNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            OperationalDashboard()
        }
        composable(Screen.Sources.route) {
            // SourcesScreen() // Placeholder as the actual screen implementation is missing
        }
        composable(Screen.CallLog.route) {
            // CallLogScreen() // Placeholder as the actual screen implementation is missing
        }
    }
}
