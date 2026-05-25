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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signalgate.multipoint.CallInfo
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
            .background(Color.Black.copy(alpha = 0.4f)) // Semi-transparent background
            .clickable(enabled = false) { } // Prevent clicks from passing through
    ) {
        callInfo?.let { info ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Caller Info
                        Text(
                            text = info.originalPhoneNumber,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Incoming Call",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Risk Level and Heartbeat
                        RiskIndicator(riskLevel = info.riskLevel ?: "UNKNOWN")

                        Spacer(modifier = Modifier.height(16.dp))

                        // Spam Status
                        Text(
                            text = info.spamStatus,
                            style = MaterialTheme.typography.titleLarge,
                            color = getStatusColor(info.riskLevel),
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (info.spamCategory != null) {
                            Text(
                                text = info.spamCategory,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Confidence Progress
                        ConfidenceSection(confidence = info.confidence ?: 0)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Matched Sources Tags
                        SourceTagsSection(sources = info.matchedSources)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ActionButton(
                                text = "Allow",
                                icon = Icons.Default.Check,
                                color = StatusLow,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onAllowClicked() }
                            )
                            ActionButton(
                                text = "Screen",
                                icon = Icons.Default.Shield,
                                color = AccentSecondary,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onScreenClicked() }
                            )
                            ActionButton(
                                text = "Block",
                                icon = Icons.Default.Block,
                                color = StatusHigh,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.onBlockClicked() }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // More Details Toggle
                        TextButton(onClick = { viewModel.toggleExpanded() }) {
                            Text(
                                text = if (isExpanded) "Less Details" else "More Details",
                                color = AccentPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = AccentPrimary
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                Divider(color = Color.White.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Detailed Analysis",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This number was flagged based on multiple reports from community feeds and telemarketer databases. Our AI engine detected high-frequency calling patterns consistent with robocalls.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
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
                shape = RoundedCornerShape(24.dp)
            )
            .blur(0.dp) // Real-time blur is expensive, using semi-transparent background for now
    ) {
        content()
    }
}

@Composable
fun RiskIndicator(riskLevel: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(getStatusColor(riskLevel).copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = getStatusColor(riskLevel),
            modifier = Modifier
                .size(16.dp)
                .scale(if (riskLevel == "HIGH") scale else 1f)
        )
        Text(
            text = "RISK LEVEL: $riskLevel",
            style = MaterialTheme.typography.labelSmall,
            color = getStatusColor(riskLevel),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ConfidenceSection(confidence: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Shield Confidence",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "$confidence%",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = confidence / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = AccentPrimary,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun SourceTagsSection(sources: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "MATCHED SOURCES",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // Simple FlowRow equivalent for Compose 1.5+
        // If FlowRow is not available in the version, we use a Row with scroll
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (sources.isEmpty()) {
                Text("None", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            } else {
                sources.take(3).forEach { source ->
                    SourceTag(source)
                }
                if (sources.size > 3) {
                    SourceTag("+${sources.size - 3}")
                }
            }
        }
    }
}

@Composable
fun SourceTag(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
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
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.2f),
            contentColor = color
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun getStatusColor(riskLevel: String?): Color {
    return when (riskLevel) {
        "HIGH" -> StatusHigh
        "MEDIUM" -> StatusMedium
        "LOW" -> StatusLow
        else -> TextMuted
    }
}
