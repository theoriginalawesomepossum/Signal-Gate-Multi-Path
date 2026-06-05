package com.signalgate.multipoint

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.signalgate.multipoint.ui.dashboard.DashboardScreen
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
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
            AppDrawer(navController = navController, closeDrawer = { scope.launch { drawerState.close() } })
        }
    ) {
        NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
            composable(Routes.DASHBOARD) {
                val dashboardViewModel: DashboardViewModel = koinViewModel()
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable(Routes.SOURCES) { TextScreen("Sources Screen") }
            composable(Routes.CALL_LOG) { TextScreen("Call Log Screen") }
            composable(Routes.BLOCK_ALLOW_LISTS) { TextScreen("Block/Allow Lists Screen") }
            composable(Routes.SETTINGS) { TextScreen("Settings Screen") }
            composable(Routes.AUDIT_LOG) { TextScreen("Audit Log Screen") }
            composable(Routes.ABOUT) { TextScreen("About Screen") }
        }
    }
}

@Composable
fun AppDrawer(navController: NavController, closeDrawer: () -> Unit) {
    // TODO: Implement the actual drawer content with navigation items
    // For now, a simple text will suffice
    Column(modifier = Modifier.fillMaxSize().background(Color.DarkGray)) {
        Text("Drawer Content", color = Color.White, modifier = Modifier.padding(16.dp))
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
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text, color = Color.White, fontSize = 24.sp)
    }
}
