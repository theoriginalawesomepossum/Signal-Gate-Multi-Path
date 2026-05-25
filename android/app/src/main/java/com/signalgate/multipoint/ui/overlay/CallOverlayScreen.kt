package com.signalgate.multipoint.ui.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.blur
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
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) { }
    ) {
        callInfo?.let { info ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top "Incoming call" label
                Text(
                    text = "Incoming call",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Phone Number
                Text(
                    text = info.originalPhoneNumber,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // Location/Country
                Text(
                    text = "United States", // Defaulting as shown in design
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Shield Logo and Identity
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
                                    tint = getStatusColor(info.riskLevel),
                                    modifier = Modifier.size(50.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = info.spamStatus,
                                    color = getStatusColor(info.riskLevel),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = info.spamCategory ?: "Unknown",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Confidence: ${info.confidence ?: 0}%",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                                LinearProgressIndicator(
                                    progress = (info.confidence ?: 0) / 100f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape),
                                    color = getStatusColor(info.riskLevel),
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Risk Level Badge
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, getStatusColor(info.riskLevel).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("RISK LEVEL", fontSize = 8.sp, color = getStatusColor(info.riskLevel))
                                        Text(info.riskLevel ?: "LOW", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = getStatusColor(info.riskLevel))
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                HeartbeatGraphic(color = getStatusColor(info.riskLevel))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Matched Sources Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Matched in ${info.matchedSources.size} sources",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Just now •",
                                color = Color.Green,
                                fontSize = 12.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            info.matchedSources.forEach { source ->
                                SourceTag(source)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionButton(
                                text = "ALLOW",
                                icon = Icons.Default.Call,
                                color = StatusLow,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onAllowClicked() }
                            )
                            ActionButton(
                                text = "SCREEN",
                                icon = Icons.Default.CastConnected,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onScreenClicked() }
                            )
                            ActionButton(
                                text = "BLOCK",
                                icon = Icons.Default.CallEnd,
                                color = StatusHigh,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onBlockClicked() }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // More Details Toggle
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.toggleExpanded() }
                                .padding(8.dp),
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
                                fontSize = 14.sp
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text(
                                    text = "Detailed threat intelligence from aggregated sources confirms high risk of fraudulent activity.",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Bottom Dial Buttons (Simulated)
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DialButton(icon = Icons.Default.Call, color = Color.Green)
                    DialButton(icon = Icons.Default.Message, color = Color.White)
                    DialButton(icon = Icons.Default.CallEnd, color = Color.Red)
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
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Icon(
        imageVector = Icons.Default.ShowChart,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(24.dp).scale(scaleX = 1f, scaleY = scale)
    )
}

@Composable
fun DialButton(icon: ImageVector, color: Color) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(if (color == Color.White) Color.White.copy(alpha = 0.2f) else color),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = if (color == Color.White) Color.White else Color.White, modifier = Modifier.size(32.dp))
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
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
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
    ) {
        content()
    }
}

@Composable
fun SourceTag(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

private fun getStatusColor(riskLevel: String?): Color {
    return when (riskLevel) {
        "HIGH" -> StatusHigh
        "MEDIUM" -> StatusMedium
        "LOW" -> StatusLow
        else -> AccentSecondary
    }
}
