package com.signalgate.multipoint

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneDisabled
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CallShieldOverlay(
    phoneNumber: String = "+1 (800) 555-0199",
    country: String = "United States",
    spamLabel: String = "LIKELY SPAM",
    spamCategory: String = "Telemarketing",
    confidence: Float = 0.92f,
    riskLevel: String = "HIGH",
    sourceTags: List<String> = listOf("Community Feed", "Telemarketer DB", "User Reports"),
    onAllowClick: () -> Unit = {},
    onScreenClick: () -> Unit = {},
    onBlockClick: () -> Unit = {},
    onMoreDetailsClick: () -> Unit = {}
) {
    var showMoreDetails by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)) // Semi-transparent background
            .blur(radius = 10.dp) // Glassmorphic blur effect
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.1f)) // Glassy background
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Incoming call",
                color = Color.White,
                fontSize = 18.sp
            )
            Text(
                text = phoneNumber,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = country,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SG Shield Logo with dynamic glow
            val infiniteTransition = rememberInfiniteTransition(label = "shieldGlow")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glowAlpha"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Blue.copy(alpha = glowAlpha), RoundedCornerShape(12.dp))
                    .padding(8.dp)
                    .background(Color.DarkGray, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("SG", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = spamLabel,
                color = Color(0xFFFFA726), // Orange color for spam
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = spamCategory,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )

            // Confidence Bar
            LinearProgressIndicator(
                progress = confidence,
                color = Color.Green,
                trackColor = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            // Risk Level with Heartbeat Graphic
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "RISK LEVEL",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = riskLevel,
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
                // Basic heartbeat animation
                val heartbeatScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "heartbeatScale"
                )
                Icon(
                    imageVector = Icons.Default.Warning, // Placeholder for heartbeat graphic
                    contentDescription = "Risk Level Indicator",
                    tint = Color.Red,
                    modifier = Modifier.padding(start = 4.dp).scale(heartbeatScale)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Source Tags
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                sourceTags.forEach { tag ->
                    Text(tag, color = Color.White, modifier = Modifier.background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = onAllowClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.7f)),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "Allow", tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("ALLOW", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onScreenClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.7f)),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.ScreenShare, contentDescription = "Screen", tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("SCREEN", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onBlockClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f)),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhoneDisabled, contentDescription = "Block", tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("BLOCK", color = Color.White)
                }
            }

            // More Details Section
            Text(
                text = if (showMoreDetails) "Less Details" else "More Details",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { showMoreDetails = !showMoreDetails }
            )

            if (showMoreDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(8.dp)
                ) {
                    Text("Full Call Details:", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Source 1: Community Feed - Matched 5 times", color = Color.White.copy(alpha = 0.8f))
                    Text("Source 2: Telemarketer DB - Last updated 2 days ago", color = Color.White.copy(alpha = 0.8f))
                    Text("Source 3: User Reports - 10 reports in last hour", color = Color.White.copy(alpha = 0.8f))
                    // Add more detailed information here
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCallShieldOverlay() {
    CallShieldOverlay()
}
