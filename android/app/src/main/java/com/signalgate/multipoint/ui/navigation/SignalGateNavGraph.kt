package com.signalgate.multipoint.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun SignalGateNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Screen.Dashboard.route) {
            Text(text = "Dashboard Content Placeholder", color = androidx.compose.ui.graphics.Color.White)
        }
        composable(Screen.Sources.route) {
            Text(text = "Data Sources Screen Placeholder", color = androidx.compose.ui.graphics.Color.White)
        }
        composable(Screen.CallLog.route) {
            Text(text = "Call Log Screen Placeholder", color = androidx.compose.ui.graphics.Color.White)
        }
        composable(Screen.BlockAllowList.route) {
            Text(text = "Block / Allow List Screen Placeholder", color = androidx.compose.ui.graphics.Color.White)
        }
        composable(Screen.Settings.route) {
            Text(text = "Settings Screen Placeholder", color = androidx.compose.ui.graphics.Color.White)
        }
    }
}
