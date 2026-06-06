package com.signalgate.multipoint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.signalgate.multipoint.OperationalDashboard
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
import com.signalgate.multipoint.ui.screens.SettingsScreen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

object Routes {
    const val DASHBOARD = "dashboard"
    const val SOURCES = "sources"
    const val CALL_LOG = "call_log"
    const val BLOCK_ALLOW_LISTS = "block_allow_lists"
    const val SETTINGS = "settings"
    const val AUDIT_LOG = "audit_log"
    const val ABOUT = "about"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navController = navController, 
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
            composable(Routes.DASHBOARD) {
                val dashboardViewModel: DashboardViewModel = koinViewModel()
                OperationalDashboard(
                    viewModel = dashboardViewModel,
                    onSettingsClick = { scope.launch { drawerState.open() } }
                )
            }
            composable(Routes.SOURCES) { TextScreen("Sources Screen") }
            composable(Routes.CALL_LOG) { TextScreen("Call Log Screen") }
            composable(Routes.BLOCK_ALLOW_LISTS) { TextScreen("Block/Allow Lists Screen") }
            
            // Your functional settings screen is now properly linked here
            composable(Routes.SETTINGS) { SettingsScreen() }
            
            composable(Routes.AUDIT_LOG) { TextScreen("Audit Log Screen") }
            composable(Routes.ABOUT) { TextScreen("About Screen") }
        }
    }
}

@Composable
fun AppDrawer(navController: NavController, closeDrawer: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(16.dp)
    ) {
        Text(
            text = "Drawer Content", 
            color = Color.White, 
            modifier = Modifier.padding(bottom = 16.dp),
            fontSize = 20.sp
        )
        Button(onClick = { navController.navigate(Routes.DASHBOARD); closeDrawer() }) { Text("Dashboard") }
        Button(onClick = { navController.navigate(Routes.SOURCES); closeDrawer() }) { Text("Sources") }
        Button(onClick = { navController.navigate(Routes.CALL_LOG); closeDrawer() }) { Text("Call Log") }
        Button(onClick = { navController.navigate(Routes.BLOCK_ALLOW_LISTS); closeDrawer() }) { Text("Block/Allow Lists") }
        Button(onClick = { navController.navigate(Routes.SETTINGS); closeDrawer() }) { Text("Settings") }
        Button(onClick = { navController.navigate(Routes.AUDIT_LOG); closeDrawer() }) { Text("Audit Log") }
        Button(onClick = { navController.navigate(Routes.ABOUT); closeDrawer() }) { Text("About") }
    }
}

@Composable
fun TextScreen(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(), 
        verticalArrangement = Arrangement.Center, 
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, color = Color.Black, fontSize = 24.sp)
    }
}
