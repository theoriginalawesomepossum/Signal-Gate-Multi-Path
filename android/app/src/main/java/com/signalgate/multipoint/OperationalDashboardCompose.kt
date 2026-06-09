package com.signalgate.multipoint

import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
import com.signalgate.multipoint.database.entities.SourceEntity
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
@Composable
fun OperationalDashboard(
    viewModel: DashboardViewModel = koinViewModel(),
    onAddSource: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val totalSources by viewModel.totalSources.collectAsState(initial = 0)
    val totalEntries by viewModel.totalEntries.collectAsState(initial = 0)
    val blockedToday by viewModel.blockedToday.collectAsState()
    val dataSources by viewModel.dataSources.collectAsState(initial = emptyList())
    val isSyncing by viewModel.isSyncing.collectAsState()

    val lastSyncTime = if (dataSources.isEmpty()) "Never" else formatLastSync(dataSources.maxOfOrNull { it.lastSynced } ?: 0)

    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .horizontalScroll(horizontalScrollState)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.widthIn(min = 600.dp)) {
            // Header with title and action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.shield_logo),
                        contentDescription = "Shield Logo",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "SIGNALGATE",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MULTI-PORT",
                            color = Color(0xFF00BCD4),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddSource,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Source", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Source", color = Color.White)
                    }
                    Button(
                        onClick = { viewModel.syncAllSources() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync All", tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync All Now", color = Color.White)
                    }
                }
            }

            // Stats Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(label = "Total Sources", value = totalSources.toString(), modifier = Modifier.weight(1f))
                StatCard(label = "Total Entries", value = totalEntries.toString(), modifier = Modifier.weight(1.2f))
                StatCard(label = "Last Sync", value = lastSyncTime, modifier = Modifier.weight(1f))
                StatCard(label = "Blocked Today", value = blockedToday.toString(), modifier = Modifier.weight(1f))
            }

            // Data Sources Section
            Text(
                text = "DATA SOURCES",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dataSources) { source ->
                    DataSourceCard(source)
                }
            }

            // Footer Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FooterStatCard(label = "Sync Schedule", value = "Every 1 hour", modifier = Modifier.weight(1f))
                FooterStatCard(label = "Database Health", value = "Good", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color(0xFF00BCD4),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DataSourceCard(source: SourceEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(160.dp)) {
            Text(
                text = source.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${source.entriesCount} entries",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.width(100.dp)) {
            Text(
                text = source.healthStatus,
                color = if (source.healthStatus == "HEALTHY") Color.Green else Color.Yellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Status",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.width(100.dp)) {
            Text(
                text = formatLastSync(source.lastSynced),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Last Synced",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp, 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Green.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color(0xFF00BCD4),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun FooterStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatLastSync(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
