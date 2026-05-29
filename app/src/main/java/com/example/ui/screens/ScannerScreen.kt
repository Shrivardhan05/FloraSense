package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Plant
import com.example.network.PlantScanResult
import com.example.ui.components.BeforeAfterSlider
import com.example.viewmodel.FloraViewModel
import com.example.viewmodel.ScanUiState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: FloraViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val isPro by viewModel.isProUser.collectAsStateWithLifecycle()
    val currentSeason by viewModel.currentAppSeason.collectAsStateWithLifecycle()

    var showPromptField by remember { mutableStateOf(false) }
    var customUserPrompt by remember { mutableStateOf("") }
    var nicknameForNewPlant by remember { mutableStateOf("") }

    // Live scanning animation helper state
    val infiniteTransition = rememberInfiniteTransition(label = "scanning_laser")
    val scanOverlayLinePct by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI FloraSense Scanner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.resetScanState()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = scanState) {
                is ScanUiState.Idle -> {
                    // IDLE state: Camera simulation frame and template triggers
                    IdleScannerPanel(
                        showPromptField = showPromptField,
                        customUserPrompt = customUserPrompt,
                        onPromptChange = { customUserPrompt = it },
                        onTogglePrompt = { showPromptField = !showPromptField },
                        onTriggerScan = { templateKey ->
                            viewModel.scanPlant(null, customUserPrompt.ifBlank { null }, templateKey)
                        }
                    )
                }

                is ScanUiState.Scanning -> {
                    // SCANNING active analysis state and sweeps
                    ScanningProgressIndicator(laserPositionPct = scanOverlayLinePct)
                }

                is ScanUiState.Success -> {
                    // SUCCESS results display with diagnostics and Before/After comparison slider
                    DiagnosticSuccessResultView(
                        scanResult = state.result,
                        isMocked = state.isMocked,
                        isPro = isPro,
                        currentSeason = currentSeason,
                        nickname = nicknameForNewPlant,
                        onNicknameChange = { nicknameForNewPlant = it },
                        onAddToGarden = { plant ->
                            viewModel.savePlantToGarden(plant)
                            viewModel.resetScanState()
                            onNavigateBack()
                        },
                        onScanAgain = {
                            viewModel.resetScanState()
                            nicknameForNewPlant = ""
                        }
                    )
                }

                is ScanUiState.Error -> {
                    // ERROR diagnostic fails
                    ScanErrorPanel(
                        message = state.message,
                        onRetry = { viewModel.resetScanState() }
                    )
                }
            }
        }
    }
}

// 1. Scanner Lens simulating viewport
@Composable
fun IdleScannerPanel(
    showPromptField: Boolean,
    customUserPrompt: String,
    onPromptChange: (String) -> Unit,
    onTogglePrompt: () -> Unit,
    onTriggerScan: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Point at plant foliage or pick a sample species template to diagnose",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Camera simulated frame
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                    .background(Color(0xFF1B2F22)),
                contentAlignment = Alignment.Center
            ) {
                // Background leaf symbol motif
                Icon(
                    imageVector = Icons.Filled.Spa,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.size(120.dp)
                )

                // Simulated digital targeting HUD bracket frame lines
                CameraHUDDecoration()

                // Tap message
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CenterFocusWeak,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Awaiting Botanical Sample",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose a test case below to inspect simulated AI responses",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        // Optional prompt customization
        item {
            Row(
                modifier = Modifier
                    .fillPaddingRow()
                    .clickable { onTogglePrompt() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (showPromptField) Icons.Filled.KeyboardArrowUp else Icons.Filled.EditNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add custom diagnostic notes (e.g., 'Repotted last week')",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (showPromptField) {
                OutlinedTextField(
                    value = customUserPrompt,
                    onValueChange = onPromptChange,
                    placeholder = { Text("Describe specific symptoms you are worried about...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("custom_scan_prompt_input")
                )
            }
        }

        // Fast diagnostic triggers database for direct simulations
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Harness Rapid Sandbox Botanical Templates",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val options = listOf(
                        Triple("Monstera (Perfect)", "monstera", Icons.Filled.Spa),
                        Triple("Fiddle Leaf Fig (Root Rot)", "fiddle", Icons.Filled.Water),
                        Triple("Snake Plant (Drought Wise)", "snake", Icons.Filled.WbSunny),
                        Triple("Boston Fern (Spider Mites)", "fern", Icons.Filled.BugReport),
                        Triple("Peace Lily (Severely Wilted)", "lily", Icons.Filled.Warning)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { option ->
                            val label = option.first
                            val key = option.second
                            val icon = option.third
                            Button(
                                onClick = { onTriggerScan(key) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("scan_trigger_$key"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(
                                        imageVector = Icons.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// Custom Row Padding util helper
fun Modifier.fillPaddingRow(): Modifier = this.fillMaxWidth().padding(vertical = 4.dp)

@Composable
fun CameraHUDDecoration() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 5.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val padding = 24.dp.toPx()

        // Top Left corner HUD
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(padding, padding),
            end = Offset(padding + cornerLength, padding),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(padding, padding),
            end = Offset(padding, padding + cornerLength),
            strokeWidth = strokeWidth
        )

        // Top Right corner HUD
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(size.width - padding, padding),
            end = Offset(size.width - padding - cornerLength, padding),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(size.width - padding, padding),
            end = Offset(size.width - padding, padding + cornerLength),
            strokeWidth = strokeWidth
        )

        // Bottom Left corner
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(padding, size.height - padding),
            end = Offset(padding + cornerLength, size.height - padding),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(padding, size.height - padding),
            end = Offset(padding, size.height - padding - cornerLength),
            strokeWidth = strokeWidth
        )

        // Bottom Right corner
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(size.width - padding, size.height - padding),
            end = Offset(size.width - padding - cornerLength, size.height - padding),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color(0xFF81C784),
            start = Offset(size.width - padding, size.height - padding),
            end = Offset(size.width - padding, size.height - padding - cornerLength),
            strokeWidth = strokeWidth
        )
    }
}

// 2. Beautiful scanning progress screens with custom sweeping laser light line
@Composable
fun ScanningProgressIndicator(laserPositionPct: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071209)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, Color(0xFF41C152))
                    .background(Color(0xFF0F2314))
            ) {
                // Background glowing pulse or laser sweep
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineY = size.height * laserPositionPct
                    drawLine(
                        color = Color(0xFF81C784),
                        start = Offset(0f, lineY),
                        end = Offset(size.width, lineY),
                        strokeWidth = 6.dp.toPx()
                    )
                }

                Icon(
                    imageVector = Icons.Filled.Spa,
                    contentDescription = null,
                    tint = Color(0xFF81C784).copy(alpha = 0.15f),
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CircularProgressIndicator(color = Color(0xFF81C784))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Harnessing Gemini AI Bot",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Deciphering stomatal structures, assessing moisture levels, and scanning leaves...",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp).padding(top = 6.dp)
            )
        }
    }
}

// 3. Scan results views with before / after slider comparison
@Composable
fun DiagnosticSuccessResultView(
    scanResult: PlantScanResult,
    isMocked: Boolean,
    isPro: Boolean,
    currentSeason: String,
    nickname: String,
    onNicknameChange: (String) -> Unit,
    onAddToGarden: (Plant) -> Unit,
    onScanAgain: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isMocked) "Static Sandbox Result Generated" else "Live Gemini AI Analysis Completed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Species heading
        item {
            Column {
                Text(
                    text = "SPECIMEN DETECTED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = scanResult.species,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Health diagnostics badge with confidence scores
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = when (scanResult.healthStatus.lowercase()) {
                            "healthy" -> Color(0xFFE8F5E9)
                            "needs care" -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("HEALTH STATE", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(
                            text = scanResult.healthStatus,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (scanResult.healthStatus.lowercase()) {
                                "healthy" -> Color(0xFF2E7D32)
                                "needs care" -> Color(0xFFEF6C00)
                                else -> Color(0xFFC62828)
                            }
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("DIAGNOSTIC CONFIDENCE", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${scanResult.confidence}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Disease description Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DIAGNOSTIC SUMMARY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (scanResult.issueDetected.lowercase() == "none") "Perfect health. Regular maintenance is recommended." else scanResult.issueDetected,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- COMPLEMENTARY SLIDER DESIGN ---
        item {
            Column {
                Text(
                    text = "RECOVERY PREDICTION (SLIDE COMPARISON)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Render customized interactive slider showing diseased vs beautiful full emerald healed foliage
                BeforeAfterSlider(
                    diseaseText = if (scanResult.issueDetected.lowercase() == "none") "Normal Foliage" else "Infected Area",
                    healthyText = "Optimized Recovery State",
                    beforeContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF3E2723), Color(0xFFD84315))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.WarningAmber,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (scanResult.healthStatus.lowercase() == "healthy") "Untouched Foliage" else "Active Symptoms Visualized",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    afterContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF1B5E20), Color(0xFF81C784))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.Eco,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Restored Healthy Emerald Leaves",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )
            }
        }

        // Color multiplier helper
        fun Color.addTintAlpha(): Color = this.copy(alpha = 0.6f)

        // Toxicity alert
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (scanResult.isPetToxic) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (scanResult.isPetToxic) Icons.Filled.Pets else Icons.Filled.VerifiedUser,
                        contentDescription = "Toxicity Check",
                        tint = if (scanResult.isPetToxic) Color(0xFFE65100) else Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (scanResult.isPetToxic) "TOXICITY ALERT" else "PET FRIENDLY GUARANTEE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (scanResult.isPetToxic) Color(0xFFE65100) else Color(0xFF1B5E20)
                        )
                        Text(
                            text = scanResult.toxicityNotes,
                            fontSize = 12.sp,
                            color = if (scanResult.isPetToxic) Color(0xFF3E2723) else Color(0xFF1B5E20),
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Step-by-step healing treatment list
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "STEP-BY-STEP TREATMENT INSTRUCTIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val stepsList = scanResult.fixes.split("\n").filter { it.isNotBlank() }
                    stepsList.forEachIndexed { idx, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (idx + 1).toString(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Add to Garden Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SAVE THIS SPECIMEN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = onNicknameChange,
                        label = { Text("E.g., Leafy Friend") },
                        placeholder = { Text("Enter custom nickname to identify easily...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_scanned_nickname_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val entity = Plant(
                                species = scanResult.species,
                                customName = nickname,
                                imageUrl = "", // Holds camera capture path standard
                                wateringIntervalDays = when (scanResult.species.lowercase()) {
                                    "monsteradeliciosa" -> 7
                                    "ficuslyrata" -> 10
                                    "sansevieriatrifasciata" -> 21
                                    "nephrolepisexaltata" -> 4
                                    else -> 7
                                },
                                lastWateredDate = System.currentTimeMillis(),
                                healthStatus = scanResult.healthStatus,
                                humidityNeed = scanResult.humidityNeed,
                                lightNeed = scanResult.lightNeed,
                                tempNeed = scanResult.tempNeed,
                                isPetToxic = scanResult.isPetToxic,
                                toxicityNotes = scanResult.toxicityNotes,
                                potSize = "Medium",
                                currentSeason = currentSeason
                            )
                            onAddToGarden(entity)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_scanned_submit_button")
                    ) {
                        Text("Add Saved Specimens into My Garden", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Button(
                onClick = onScanAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("new_scan_trigger"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Discard and Retake Scan", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// 4. Scan Error layout
@Composable
fun ScanErrorPanel(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Diagnostic Scan Error",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.testTag("error_retry_button")
            ) {
                Text("Try Scan Again")
            }
        }
    }
}
