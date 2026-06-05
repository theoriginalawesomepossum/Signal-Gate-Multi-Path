package com.signalgate.multipoint.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.signalgate.multipoint.data.models.SignalGateMode
import com.signalgate.multipoint.data.models.BenchmarkResult
import com.signalgate.multipoint.ui.components.AdvancedGlassCard
import com.signalgate.multipoint.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MultiPortHubScreen(modifier: Modifier = Modifier) {
    var currentMode by remember { mutableStateOf(SignalGateMode.MULTI_GATE) }
    var showModeSelector by remember { mutableStateOf(false) }
    var isRunningBenchmark by remember { mutableStateOf(false) }
    
    var benchmarkResult by remember { 
        mutableStateOf<BenchmarkResult?>(BenchmarkResult(12, 384, 45, true, "OPTIMAL")) 
    }
    
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTION 1: SIGNALGATE SIGNATURE STATUS CARD ---
        item {
            AdvancedGlassCard(
                modifier = Modifier.clickable { showModeSelector = !showModeSelector }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SIGNALGATE STATE", color = TextSecondary, fontSize = 11.sp)
                        Text(currentMode.title, color = NeonCyan, fontSize = 22.sp, style = MaterialTheme.typography.headlineMedium)
                        Text("Mode Target: ${currentMode.label}", color = TextPrimary, fontSize = 12.sp)
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(SurfaceGlass, shape = RoundedCornerShape(8.dp))
                            .border(1.dp, NeonCyan, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("CHANGE", color = NeonCyan, fontSize = 12.sp)
                    }
                }
            }
        }

        // --- SECTION 2: DYNAMIC MODE SELECTOR & BENCHMARK MODULE ---
        item {
            AnimatedVisibility(visible = showModeSelector) {
                AdvancedGlassCard {
                    Text("MODE SELECTOR ENGINE", color = TextPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    SignalGateMode.values().forEach { mode ->
                        val isSelected = currentMode == mode
                        val isSupported = mode != SignalGateMode.FULL_THROTTLE || (benchmarkResult?.isFullThrottleSupported == true)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceGlass else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NeonCyan else if (!isSupported) NeonRed.copy(alpha = 0.3f) else BorderGlass.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = isSupported) { currentMode = mode }
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = mode.title, 
                                        color = if (isSelected) NeonCyan else if (!isSupported) TextSecondary.copy(alpha = 0.5f) else TextPrimary,
                                        fontSize = 15.sp
                                    )
                                    if (!isSupported) {
                                        Text("UNSUPPORTED BY HARDWARE", color = NeonRed, fontSize = 10.sp)
                                    }
                                }
                                Text(
                                    text = mode.description, 
                                    color = TextSecondary, 
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = BorderGlass, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // --- BENCHMARK TELEMETRY RENDER ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TELEMETRY BENCHMARK", color = TextPrimary, fontSize = 13.sp)
                            benchmarkResult?.let {
                                Text("Storage I/O Seek: ${it.ioReadSpeedMs}ms", color = TextSecondary, fontSize = 11.sp)
                                Text("Available App Heap: ${it.availableMemoryMb}MB", color = TextSecondary, fontSize = 11.sp)
                                Text("Hardware Profile: ${it.scoreText}", color = if (it.isFullThrottleSupported) NeonGreen else NeonOrange, fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = {
                                isRunningBenchmark = true
                                scope.launch {
                                    delay(2000) // Simulate live I/O stress testing
                                    benchmarkResult = BenchmarkResult(8, 512, 60, true, "PERFECT")
                                    isRunningBenchmark = false
                                }
                            },
                            enabled = !isRunningBenchmark,
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass)
                        ) {
                            Text(if (isRunningBenchmark) "TESTING..." else "RUN TEST", color = NeonCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- SECTION 3: INGESTION SOURCE CONFIGURATOR ---
        item {
            Text("DATA INGESTION CHANNELS", color = TextPrimary, fontSize = 14.sp)
        }

        item {
            AdvancedGlassCard {
                Text("Local Directory Porting", color = TextPrimary, fontSize = 16.sp)
                Text("Ingest local .csv file structures directly from device downloads storage domain.", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Launch File Picker Intent */ },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass)
                ) {
                    Text("Select Local CSV", color = NeonCyan)
                }
            }
        }

        item {
            AdvancedGlassCard {
                Text("Remote Stream Configuration", color = TextPrimary, fontSize = 16.sp)
                Text("Sync directly against the SignalGate Community GitHub repository feed or secure private Cloud providers.", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
                
                var feedUrl by remember { mutableStateOf("https://github.com/signalgate/community-feeds") }
                OutlinedTextField(
                    value = feedUrl,
                    onValueChange = { feedUrl = it },
                    label = { Text("Stream Source Endpoint URL", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = BorderGlass,
                        focusedLabelColor = NeonCyan
                    )
                )
            }
        }
    }
}