package com.signalgate.multipoint.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.signalgate.multipoint.data.models.PermissionStatus
import com.signalgate.multipoint.ui.components.AdvancedGlassCard
import com.signalgate.multipoint.ui.theme.*

@Composable
fun PermissionSettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // List of manual permissions critical for a premium Call Screening application
    val trackedPermissions = remember {
        listOf(
            PermissionStatus(
                "Role: Call Screening",
                android.Manifest.permission.ANSWER_PHONE_CALLS, // Proxy descriptor
                "Required to parse and intercept incoming multi-port vectors natively.",
                isGranted = false, // Managed conditionally below
                isRequiredForCoreFunction = true
            ),
            PermissionStatus(
                "Read Phone State",
                android.Manifest.permission.READ_PHONE_STATE,
                "Allows verification of operational cellular band telemetry.",
                isGranted = false,
                isRequiredForCoreFunction = true
            ),
            PermissionStatus(
                "Read Call Log",
                android.Manifest.permission.READ_CALL_LOG,
                "Populates the real-time advanced telemetry history dashboard.",
                isGranted = false,
                isRequiredForCoreFunction = false
            )
        )
    }

    // Dynamic verification state
    var permissionsState by remember { mutableStateOf(trackedPermissions) }

    // Audit current system permission alignment whenever the screen is viewed
    LaunchedEffect(Unit) {
        permissionsState = trackedPermissions.map { permission ->
            val granted = ContextCompat.checkSelfPermission(
                context, 
                permission.manifestString
            ) == PackageManager.PERMISSION_GRANTED
            permission.copy(isGranted = granted)
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "SYSTEM ACCESS COMPLIANCE",
            color = TextPrimary,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Audit and toggle low-level hardware security hooks below.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(permissionsState) { permission ->
                AdvancedGlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(permission.permissionName, color = TextPrimary, fontSize = 16.sp)
                                if (permission.isRequiredForCoreFunction) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("[CORE]", color = NeonRed, fontSize = 10.sp)
                                }
                            }
                            Text(permission.description, color = TextSecondary, fontSize = 12.sp)
                        }

                        Switch(
                            checked = permission.isGranted,
                            onCheckedChange = {
                                // Guide the prosumer securely to app system controls to manually adjust permissions
                                openApplicationSettings(context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = SurfaceGlass,
                                uncheckedThumbColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun openApplicationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}