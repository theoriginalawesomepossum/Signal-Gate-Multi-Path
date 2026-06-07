package com.signalgate.multipoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalgate.multipoint.data.models.SourceStatus
import com.signalgate.multipoint.data.models.SourceType
import com.signalgate.multipoint.data.models.ThreatSource
import com.signalgate.multipoint.ui.components.GlassmorphicCard
import com.signalgate.multipoint.ui.theme.*

@Composable
fun SourcesScreen(modifier: Modifier = Modifier) {
    // Mocking state that would typically flow from your Koin-injected DataSourceRepository
    val sourceList = remember {
        mutableStateListOf(
            ThreatSource("1", "Community Spam Feed", SourceType.REMOTE_URL, 127854, SourceStatus.HEALTHY, "Success", "1m ago", true),
            ThreatSource("2", "Personal Block List", SourceType.LOCAL_CSV, 24610, SourceStatus.HEALTHY, "Success", "5m ago", true),
            ThreatSource("3", "Telemarketer Database", SourceType.REMOTE_URL, 212331, SourceStatus.HEALTHY, "Success", "3m ago", true),
            ThreatSource("4", "User Reports Feed", SourceType.REMOTE_URL, 46122, SourceStatus.ERROR, "Timeout", "23m ago", true)
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("DATA SOURCES", color = TextPrimary, fontSize = 18.sp, style = MaterialTheme.typography.titleMedium)
            Button(
                onClick = { /* Action to add source */ },
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass)
            ) {
                Text("+ Add Source", color = NeonCyan)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = sourceList, key = { it.id }) { source ->
                GlassmorphicCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(source.name, color = TextPrimary, fontSize = 16.sp)
                            Text(source.type.name.replace("_", " "), color = TextSecondary, fontSize = 12.sp)
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${source.entriesCount} Entries", color = TextPrimary, fontSize = 14.sp)
                            
                            val statusColor = when (source.status) {
                                SourceStatus.HEALTHY -> NeonGreen
                                SourceStatus.ERROR -> NeonRed
                                SourceStatus.DISABLED -> TextSecondary
                            }
                            Text(source.status.name, color = statusColor, fontSize = 12.sp)
                        }

                        Switch(
                            checked = source.isEnabled,
                            onCheckedChange = { checked ->
                                val index = sourceList.indexOf(source)
                                if (index != -1) {
                                    sourceList[index] = source.copy(isEnabled = checked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = SurfaceGlass
                            )
                        )
                    }
                }
            }
        }
    }
}
