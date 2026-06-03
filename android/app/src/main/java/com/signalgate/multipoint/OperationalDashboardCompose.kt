package com.signalgate.multipoint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signalgate.multipoint.ui.dashboard.DashboardViewModel
import com.signalgate.multipoint.database.entities.SourceEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OperationalDashboard(
    viewModel: DashboardViewModel = viewModel(),
    onAddSource: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val totalSources by viewModel.totalSources.collectAsState(initial = 0)
    val totalEntries by viewModel.totalEntries.collectAsState(initial = 0)
    val blockedToday by viewModel.blockedToday.collectAsState()
    val dataSources by viewModel.dataSources.collectAsState(initial = emptyList())
    val isSyncing by viewModel.isSyncing.collectAsState()

    val lastSyncTime = if (dataSources.isEmpty()) "Never" else formatLastSync(dataSources.maxOfOrNull { it.lastSynced } ?: 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a)) // Dark background
            .padding(16.dp)
    ) {
        // Header with title and action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SIGNALGATE MULTI-PORT",
                color = Color(0xFF00BCD4), // Cyan color
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
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
            StatCard(label = "Total Entries", value = totalEntries.toString(), modifier = Modifier.weight(1f))
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
            FooterStatCard(label = "Benchmark Mode", value = "CENTER-PORT", modifier = Modifier.weight(1f))
            FooterStatCard(label = "Sync Schedule", value = "Every 1 hour", modifier = Modifier.weight(1f))
            FooterStatCard(label = "Database Health", value = "Good", modifier = Modifier.weight(1f))
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
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = source.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${source.entriesCount} entries",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = source.healthStatus,
                color = if (source.healthStatus == "HEALTHY") Color.Green else Color.Yellow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatLastSync(source.lastSynced),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
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
