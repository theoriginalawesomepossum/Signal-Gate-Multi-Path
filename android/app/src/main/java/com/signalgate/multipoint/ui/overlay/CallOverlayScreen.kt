package com.signalgate.multipoint.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signalgate.multipoint.CallInfo
import com.signalgate.multipoint.R
import com.signalgate.multipoint.ui.theme.*

@Composable
fun CallOverlayScreen(
    viewModel: CallOverlayViewModel = viewModel(),
    onDismiss: () -> Unit = {}
) {
    val callInfo by viewModel.callInfo.collectAsState()
    val isExpanded by viewModel.isExpanded.collectAsState()
    val dismissOverlay by viewModel.dismissOverlay.collectAsState()

    LaunchedEffect(dismissOverlay) {
        if (dismissOverlay) {
            onDismiss()
            viewModel.onDismissed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(enabled = false) { }
    ) {
        callInfo?.let { info ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top "Incoming call" label
                Text(
                    text = "Incoming call",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Phone Number
                Text(
                    text = info.originalPhoneNumber,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // Location/Country
                Text(
                    text = "United States",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                GlassmorphicOverlayCard(
                    riskLevel = info.riskLevel ?: "LOW",
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shield Logo
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.shield_logo),
                                    contentDescription = null,
                                    tint = Color.Unspecified, // Keep original colors if it's a multi-color drawable
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = info.spamStatus.uppercase(),
                                    color = getRiskColor(info.riskLevel),
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = info.spamCategory ?: "Unknown",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Confidence: ${info.confidence ?: 0}%",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                LinearProgressIndicator(
                                    progress = (info.confidence ?: 0) / 100f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = getRiskColor(info.riskLevel),
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Risk Level Badge
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, getRiskColor(info.riskLevel).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("RISK LEVEL", fontSize = 9.sp, color = getRiskColor(info.riskLevel), fontWeight = FontWeight.Bold)
                                        Text(info.riskLevel?.uppercase() ?: "LOW", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = getRiskColor(info.riskLevel))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                HeartbeatGraphic(color = getRiskColor(info.riskLevel))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Matched Sources Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Matched in ${info.matchedSources.size} sources",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Just now",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Green))
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Using a simple Row for tags as shown in prototype
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            info.matchedSources.take(3).forEach { source ->
                                SourceTag(source)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OverlayActionButton(
                                text = "ALLOW",
                                icon = Icons.Default.Call,
                                color = StatusLow,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onAllowClicked() }
                            )
                            OverlayActionButton(
                                text = "SCREEN",
                                icon = Icons.Default.CastConnected,
                                color = Color(0xFF4A5568), // Slate gray for screen
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onScreenClicked() }
                            )
                            OverlayActionButton(
                                text = "BLOCK",
                                icon = Icons.Default.CallEnd,
                                color = StatusHigh,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onBlockClicked() }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // More Details Toggle
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.toggleExpanded() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(Color.White.copy(alpha = 0.05f)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "More Details",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                                Text(
                                    text = "Detailed threat intelligence from aggregated sources confirms high risk of fraudulent activity.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Bottom Dial Buttons (Simulated)
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DialButton(icon = Icons.Default.Call, color = Color(0xFF4CAF50))
                    DialButton(icon = Icons.Default.Call, color = Color.White) // Middle green call button
                    DialButton(icon = Icons.Default.CallEnd, color = Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
fun HeartbeatGraphic(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Icon(
        imageVector = Icons.Default.ShowChart,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(28.dp).scale(scaleX = 1f, scaleY = scale)
    )
}

@Composable
fun DialButton(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (color == Color.White) Color.White.copy(alpha = 0.15f) else color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            tint = if (color == Color.White) Color(0xFF4CAF50) else Color.White, 
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun SourceTag(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = name,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun OverlayActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun GlassmorphicOverlayCard(
    riskLevel: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val glowColor = getRiskColor(riskLevel).copy(alpha = 0.1f)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        // Subtle glow effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(glowColor, Color.Transparent),
                        radius = 800f
                    )
                )
        )
        content()
    }
}

fun getRiskColor(riskLevel: String?): Color {
    return when (riskLevel?.uppercase()) {
        "HIGH" -> StatusHigh
        "MEDIUM" -> StatusMedium
        "LOW" -> StatusLow
        else -> StatusLow
    }
}
