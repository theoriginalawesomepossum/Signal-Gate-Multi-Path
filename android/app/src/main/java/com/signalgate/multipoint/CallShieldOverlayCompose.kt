package com.signalgate.multipoint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CallShieldOverlay() {
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
                text = "+1 (800) 555-0199", // Placeholder for dynamic number
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "United States", // Placeholder for dynamic country
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SG Shield Logo (Placeholder)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("SG", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "LIKELY SPAM",
                color = Color(0xFFFFA726), // Orange color for spam
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Telemarketing",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )

            // Confidence Bar (Placeholder)
            LinearProgressIndicator(
                progress = 0.92f, // Placeholder for dynamic confidence
                color = Color.Green,
                trackColor = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            // Risk Level (Placeholder)
            Text(
                text = "RISK LEVEL HIGH",
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Source Tags (Placeholder)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Community Feed", color = Color.White, modifier = Modifier.background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
                Text("Telemarketer DB", color = Color.White, modifier = Modifier.background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
                Text("User Reports", color = Color.White, modifier = Modifier.background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Placeholder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(onClick = { /* TODO: Handle Allow */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.7f))) {
                    Text("ALLOW", color = Color.White)
                }
                Button(onClick = { /* TODO: Handle Screen */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.7f))) {
                    Text("SCREEN", color = Color.White)
                }
                Button(onClick = { /* TODO: Handle Block */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))) {
                    Text("BLOCK", color = Color.White)
                }
            }

            Text(
                text = "More Details",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewCallShieldOverlay() {
    CallShieldOverlay()
}
