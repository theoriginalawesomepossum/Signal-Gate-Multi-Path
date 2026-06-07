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
import com.signalgate.multipoint.data.models.CallLogItem
import com.signalgate.multipoint.data.models.CallType
import com.signalgate.multipoint.ui.components.GlassmorphicCard
import com.signalgate.multipoint.ui.theme.*

@Composable
fun CallLogScreen(modifier: Modifier = Modifier) {
    // In production, wire this via Koin using koinViewModel() linking to your CallScreeningService database records
    val callLogs = remember {
        listOf(
            CallLogItem("1", "+1 (800) 555-0199", "United States", "Just now", CallType.SPAM, listOf("Community Feed", "Telemarketer DB"), 92),
            CallLogItem("2", "+1 (555) 014-4821", "California", "15m ago", CallType.INCOMING, emptyList(), 0),
            CallLogItem("3", "+1 (888) 234-9912", "Unknown Location", "1h ago", CallType.BLOCKED, listOf("Personal Block List"), 100)
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "TELEMETRY CALL LOG",
            color = TextPrimary,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items = callLogs, key = { it.id }) { log ->
                GlassmorphicCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(log.phoneNumber, color = TextPrimary, fontSize = 16.sp)
                            Text("${log.location} • ${log.timestamp}", color = TextSecondary, fontSize = 12.sp)
                            
                            if (log.matchedSources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Matched: ${log.matchedSources.joinToString(", ")}",
                                    color = NeonOrange,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val typeColor = when (log.type) {
                                CallType.SPAM -> NeonOrange
                                CallType.BLOCKED -> NeonRed
                                CallType.INCOMING -> NeonGreen
                                CallType.OUTGOING -> NeonCyan
                            }
                            Text(log.type.name, color = typeColor, fontSize = 14.sp)
                            
                            if (log.riskConfidence > 0) {
                                Text("Risk: ${log.riskConfidence}%", color = NeonRed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
