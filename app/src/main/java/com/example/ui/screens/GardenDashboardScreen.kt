package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Plant
import com.example.viewmodel.FloraViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.testTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GardenDashboardScreen(
    viewModel: FloraViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToPro: () -> Unit,
    modifier: Modifier = Modifier
) {
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val currentSeason by viewModel.currentAppSeason.collectAsStateWithLifecycle()
    val isPro by viewModel.isProUser.collectAsStateWithLifecycle()

    var showAddQuickPlantDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Spa,
                            contentDescription = "FloraSense Logo",
                            tint = Color(0xFF81C784),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "FloraSense Garden",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToPro,
                        modifier = Modifier.testTag("pro_badge_button")
                    ) {
                        Icon(
                            imageVector = if (isPro) Icons.Filled.MilitaryTech else Icons.Outlined.MilitaryTech,
                            contentDescription = "Pro Toggle",
                            tint = if (isPro) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .testTag("add_to_garden_fab")
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Scan New")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Identify & Diagnose", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Adaptive Season Toggle Panel
            item {
                SeasonToggleCard(
                    activeSeason = currentSeason,
                    onSeasonChange = { viewModel.setSeason(it) }
                )
            }

            // High Fidelity Garden Statistics panel
            item {
                GardenSummaryCard(plants = plants, isPro = isPro, currentSeason = currentSeason)
            }

            // Header for plant grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Collection (${plants.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TextButton(
                        onClick = { showAddQuickPlantDialog = true },
                        modifier = Modifier.testTag("quick_add_text_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Quick Add")
                    }
                }
            }

            // Plants or Empty state list representation
            if (plants.isEmpty()) {
                item {
                    EmptyGardenState(onScanNavigate = onNavigateToScan)
                }
            } else {
                items(plants, key = { it.id }) { plant ->
                    PlantCareItemCard(
                        plant = plant,
                        isPro = isPro,
                        onWater = { viewModel.waterPlant(plant) },
                        onRemove = { viewModel.removePlant(plant) },
                        onSaveNotes = { note -> viewModel.updatePlantJournal(plant, note) }
                    )
                }
            }

            // Padding at bottom for floating action button offset
            item {
                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }

    // Quick Add plant manually dialog
    if (showAddQuickPlantDialog) {
        QuickAddPlantDialog(
            currentSeason = currentSeason,
            onDismiss = { showAddQuickPlantDialog = false },
            onSave = { plant ->
                viewModel.savePlantToGarden(plant)
                showAddQuickPlantDialog = false
            }
        )
    }
}

@Composable
fun SeasonToggleCard(
    activeSeason: String,
    onSeasonChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.WbSunny,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Adaptive Season Chamber",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Toggle seasons to trigger mathematical water-reabsorption adjustments live in the grid!",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            val seasons = listOf("Spring", "Summer", "Autumn", "Winter")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                seasons.forEach { season ->
                    val isSelected = season == activeSeason
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface
                            )
                            .clickable { onSeasonChange(season) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = season,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GardenSummaryCard(
    plants: List<Plant>,
    isPro: Boolean,
    currentSeason: String
) {
    val totalCount = plants.size
    val standardDue = plants.count { it.getDaysUntilWatering() <= 0 }

    val hydrationPercentage = if (totalCount == 0) 100 else {
        ((totalCount - standardDue).toFloat() / totalCount.toFloat() * 100).toInt()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Care",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (standardDue > 0) "$standardDue plants need watering today" else "All plants are fully hydrated",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Watering",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                RoundedCornerShape(100.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Misting",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    strokeWidth = 4.dp,
                )
                CircularProgressIndicator(
                    progress = { hydrationPercentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp,
                )
                Text(
                    text = "$hydrationPercentage%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun EmptyGardenState(onScanNavigate: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Welcome to FloraSense",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your garden is currently empty. Use the scanning diagnostic tool to instantly identify types and check for diseases!",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onScanNavigate,
                modifier = Modifier.testTag("scan_first_button")
            ) {
                Icon(Icons.Filled.Camera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Your First Plant")
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlantCareItemCard(
    plant: Plant,
    isPro: Boolean,
    onWater: () -> Unit,
    onRemove: () -> Unit,
    onSaveNotes: (String) -> Unit
) {
    var expandedNotes by remember { mutableStateOf(false) }
    var currentJournalNote by remember { mutableStateOf(plant.note) }

    // Calculate dynamic state
    val daysLeft = plant.getDaysUntilWatering()
    val isOverdue = daysLeft <= 0

    val textOverdue = if (isOverdue) "Overdue!" else "Due in $daysLeft ${if (daysLeft == 1) "day" else "days"}"
    val containerColor = if (isOverdue) Color(0xFFFFECEF) else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plant_item_${plant.id}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOverdue) Color(0xFFC62828).copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Image Placeholder/Foliage Icon + Title and water action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High contrast fallback botanical icon container with custom colors
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isOverdue) Color(0xFFFFCDD2)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (plant.healthStatus.lowercase() == "healthy") Icons.Filled.Spa else Icons.Filled.Warning,
                        contentDescription = "Foliage Icon",
                        tint = if (isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1.0f)) {
                    Text(
                        text = plant.customName.ifBlank { plant.species },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (plant.customName.isNotBlank()) {
                        Text(
                            text = plant.species,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Status: ${plant.healthStatus}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (plant.healthStatus.lowercase()) {
                            "healthy" -> Color(0xFF2E7D32)
                            "needs care" -> Color(0xFFEF6C00)
                            else -> Color(0xFFC62828)
                        }
                    )
                }

                // Interactive tap component
                IconButton(
                    onClick = onWater,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isOverdue) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                        )
                        .testTag("water_button_${plant.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = "Water plant",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Adaptive Reminder Countdown Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassEmpty,
                        contentDescription = null,
                        tint = if (isOverdue) Color(0xFFC62828) else Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = textOverdue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverdue) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Adaptive badges indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isPro) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isPro) "Adaptive Limit: ON" else "Standard (Pro off)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPro) Color(0xFF2E7D32) else Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Info rows displaying Humidity, Light, and Temp requirements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RequirementBadge(
                    icon = Icons.Outlined.Water,
                    label = "Water",
                    value = "${plant.wateringIntervalDays}d",
                    modifier = Modifier.weight(1f)
                )
                RequirementBadge(
                    icon = Icons.Outlined.WbSunny,
                    label = "Light",
                    value = plant.lightNeed,
                    modifier = Modifier.weight(1.2f)
                )
                RequirementBadge(
                    icon = Icons.Outlined.DeviceThermostat,
                    label = "Temp",
                    value = plant.tempNeed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pet Toxicity alerts bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (plant.isPetToxic) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (plant.isPetToxic) Icons.Filled.Pets else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (plant.isPetToxic) Color(0xFFEF6C00) else Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (plant.isPetToxic) "Toxic to cats & dogs" else "100% Pet Friendly",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (plant.isPetToxic) Color(0xFFE65100) else Color(0xFF1B5E20)
                    )
                    if (plant.isPetToxic && plant.toxicityNotes.isNotBlank()) {
                        Text(
                            text = plant.toxicityNotes,
                            fontSize = 9.sp,
                            color = Color(0xFFE65100).copy(alpha = 0.8f),
                            lineHeight = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable Journal note editor section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedNotes = !expandedNotes }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expandedNotes) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (plant.note.isBlank()) "Add progress notes & journals" else "Journals: ${plant.note.take(28)}...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(
                visible = expandedNotes,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = currentJournalNote,
                        onValueChange = { currentJournalNote = it },
                        placeholder = { Text("Write notes about repotting dates or leaf changes...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(vertical = 6.dp)
                            .testTag("journal_input_${plant.id}"),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onRemove,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFC62828))
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 11.sp)
                        }

                        TextButton(
                            onClick = { onSaveNotes(currentJournalNote) }
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Journal", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequirementBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddPlantDialog(
    currentSeason: String,
    onDismiss: () -> Unit,
    onSave: (Plant) -> Unit
) {
    var species by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    var baseInterval by remember { mutableStateOf("7") }
    var lightRequirement by remember { mutableStateOf("Indirect Light") }
    var humidityRequirement by remember { mutableStateOf("Medium") }
    var temperatureSetting by remember { mutableStateOf("18-24°C") }
    var targetPetToxic by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add Base Plant") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = species,
                    onValueChange = { species = it },
                    label = { Text("Species Name") },
                    modifier = Modifier.fillMaxWidth().testTag("add_species_input")
                )

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Nick Name (Optional)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_custom_name_input")
                )

                OutlinedTextField(
                    value = baseInterval,
                    onValueChange = { baseInterval = it },
                    label = { Text("Base Water Interval (Days)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_interval_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = targetPetToxic,
                        onCheckedChange = { targetPetToxic = it },
                        modifier = Modifier.testTag("add_toxic_checkbox")
                    )
                    Text("Toxic to Pets (Caution Alert)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val days = baseInterval.toIntOrNull() ?: 7
                    val entity = Plant(
                        species = species.ifBlank { "Unknown Species" },
                        customName = customName,
                        imageUrl = "",
                        wateringIntervalDays = days,
                        lastWateredDate = System.currentTimeMillis(),
                        healthStatus = "Healthy",
                        humidityNeed = humidityRequirement,
                        lightNeed = lightRequirement,
                        tempNeed = temperatureSetting,
                        isPetToxic = targetPetToxic,
                        toxicityNotes = if (targetPetToxic) "Calcium oxalate irritants present." else "Safe for canine and feline proximity.",
                        potSize = "Medium",
                        currentSeason = currentSeason
                    )
                    onSave(entity)
                },
                modifier = Modifier.testTag("save_plant_confirm")
            ) {
                Text("Save to Garden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
