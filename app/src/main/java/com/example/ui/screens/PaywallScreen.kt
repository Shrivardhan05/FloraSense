package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.FloraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    viewModel: FloraViewModel,
    modifier: Modifier = Modifier
) {
    val isPro by viewModel.isProUser.collectAsStateWithLifecycle()
    var selectedPlanIndex by remember { mutableStateOf(0) } // 0 = Monthly, 1 = Lifetime

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FloraSense Premium", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Premium Crown Icon and header
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFBC02D), Color(0xFFFFA000))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MilitaryTech,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Unleash Intelligent Care",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Upgrade to Pro to automate watering calculations based on real-time seasons, soil porosity, and pot density",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Comparative bullet points of what Pro is offering
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProBulletPoint(
                    icon = Icons.Filled.CloudUpload,
                    title = "Unlimited Gemini AI Diagnostics",
                    desc = "Real-time scans decipher thousands of pests, fungi, and watering deficiencies in seconds."
                )
                ProBulletPoint(
                    icon = Icons.Filled.AutoFixHigh,
                    title = "Sensory Adaptive Reminders",
                    desc = "Watering alarms adapt instantly to current weather, pot thickness, and seasonal cycles."
                )
                ProBulletPoint(
                    icon = Icons.Filled.WbCloudy,
                    title = "Humidity & Microclimate Sync",
                    desc = "Combines ambient local air moisture levels to secure ideal root hydration levels."
                )
                ProBulletPoint(
                    icon = Icons.Filled.Wallpaper,
                    title = "Home Screen Care Widgets",
                    desc = "Convenient Lock Screen alerts prompt action for upcoming dry spells instantly."
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subscription Plan Choice Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Plan 1: Monthly
                PlanSelectionCard(
                    title = "Monthly Pro Plan",
                    price = "$4.99 / mo",
                    desc = "Cancel anytime. Billed monthly.",
                    isSelected = selectedPlanIndex == 0,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedPlanIndex = 0 }
                        .testTag("plan_monthly_card")
                )

                // Plan 2: Lifetime (Best Deal)
                PlanSelectionCard(
                    title = "Lifetime Greenery",
                    price = "$29.99 once",
                    desc = "Pay once. Own forever.",
                    isSelected = selectedPlanIndex == 1,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedPlanIndex = 1 }
                        .testTag("plan_lifetime_card")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Premium Purchase Button Trigger
            Button(
                onClick = { viewModel.toggleProUser() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("purchase_action_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPro) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPro) Icons.Filled.CancelPresentation else Icons.Filled.LockOpen,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPro) "Deactivate Premium Sandbox (Revert)" else "Activate Premium Plan Simulator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Current status banner
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isPro) Color(0xFFE8F5E9) else Color(0xFFECEFF1)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "STATUS: " + (if (isPro) "PRO ACTIVE (Adaptive reminders unlocked)" else "BASIC FREE TIER"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPro) Color(0xFF2E7D32) else Color(0xFF455A64)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun ProBulletPoint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(desc, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 12.sp)
        }
    }
}

@Composable
fun PlanSelectionCard(
    title: String,
    price: String,
    desc: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val borderThickness = 1.dp
    val containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .border(borderThickness, borderColor, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = price,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = desc,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp
            )
        }
    }
}
