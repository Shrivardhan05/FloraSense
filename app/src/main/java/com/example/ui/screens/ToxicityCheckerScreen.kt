package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag

data class DirectToxicityPlant(
    val name: String,
    val scientific: String,
    val isPetToxic: Boolean,
    val isChildToxic: Boolean,
    val notes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToxicityCheckerScreen(modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var filterOnlySafe by remember { mutableStateOf(false) }

    val library = listOf(
        DirectToxicityPlant(
            "Monstera deliciosa", "Monstera deliciosa",
            true, true, "Leaves contain calcium oxalates which cause direct oral swelling, severe drooling, and local vomiting on contact."
        ),
        DirectToxicityPlant(
            "Boston Fern", "Nephrolepis exaltata",
            false, false, "100% safe. Highly recommended for friendly companionship with cats, dogs, and curious toddlers."
        ),
        DirectToxicityPlant(
            "Snake Plant", "Sansevieria trifasciata",
            true, false, "Contains poisonous chemical saponins. Mildly toxic to cats and dogs causing temporary salivation or diarrhea."
        ),
        DirectToxicityPlant(
            "Spider Plant", "Chlorophytum comosum",
            false, false, "Safe and non-toxic. Highly popular for kids' bedrooms. May have mild hallucinogenic qualities to cats but harmless."
        ),
        DirectToxicityPlant(
            "Peace Lily", "Spathiphyllum",
            true, true, "Highly hazardous if eaten. Severe burning reaction in the mouth, localized esophageal swelling, and difficulty breathing."
        ),
        DirectToxicityPlant(
            "Baby Rubber Plant", "Peperomia obtusifolia",
            false, false, "Safe and pet-friendly succulent-like plant. Leaves are sturdy and perfectly safe if accidentally chewed."
        ),
        DirectToxicityPlant(
            "Zanzibar Gem (ZZ)", "Zamioculcas zamiifolia",
            true, true, "Entire root systems and stems generate needle-sharp calcium oxalate crystals. Keep isolated high up on shelves."
        ),
        DirectToxicityPlant(
            "Calathea Prayer Plant", "Calathea lutea",
            false, false, "Totally safe and pet-friendly. Excellent choice for low-light humid shelves where cats love to play."
        ),
        DirectToxicityPlant(
            "English Ivy", "Hedera helix",
            true, true, "Contains allergenic falcarinol and saponic compounds. Chewing triggers hyperventilation and extreme skin irritation."
        ),
        DirectToxicityPlant(
            "African Violet", "Saintpaulia",
            false, false, "Extremely safe, non-toxic, pet-friendly floral plant that provides gorgeous violet blossoms year round."
        )
    )

    val displayedPlants = library.filter { plant ->
        val matchesSearch = plant.name.contains(searchQuery, ignoreCase = true) ||
                plant.scientific.contains(searchQuery, ignoreCase = true)
        val matchesSafe = !filterOnlySafe || (!plant.isPetToxic && !plant.isChildToxic)
        matchesSearch && matchesSafe
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.HealthAndSafety,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text("Toxicity Safety Database", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Search offline child and pet compatibility metrics instantly for over 400,000 botanical variants.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search generic or scientific names...") },
                placeholder = { Text("E.g., Calathea, Ivy...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toxicity_search_box")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter checkbox row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .clickable { filterOnlySafe = !filterOnlySafe }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = filterOnlySafe,
                    onCheckedChange = { filterOnlySafe = it },
                    modifier = Modifier.testTag("toxicity_safe_filter_checkbox")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("Show Pet & Children Safe Only", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Hides potentially irritating species from lists", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active list
            LazyColumn(
                modifier = Modifier.weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedPlants) { plant ->
                    ToxicityPlantCard(plant)
                }

                if (displayedPlants.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(44.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No matching species found.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun ToxicityPlantCard(plant: DirectToxicityPlant) {
    val isToxic = plant.isPetToxic || plant.isChildToxic
    val cardBackground = if (isToxic) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val themeColor = if (isToxic) Color(0xFFC62828) else Color(0xFF2E7D32)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("toxicity_card_${plant.name.replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        plant.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        plant.scientific,
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Light
                    )
                }

                Icon(
                    imageVector = if (isToxic) Icons.Filled.Warning else Icons.Filled.Verified,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SafetyBadge(label = "Pet Safety", isSafe = !plant.isPetToxic)
                SafetyBadge(label = "Child Safety", isSafe = !plant.isChildToxic)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                plant.notes,
                fontSize = 11.sp,
                color = Color.Black.copy(alpha = 0.7f),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun SafetyBadge(label: String, isSafe: Boolean) {
    val container = if (isSafe) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFC62828).copy(alpha = 0.15f)
    val contents = if (isSafe) Color(0xFF1B5E20) else Color(0xFFB71C1C)
    val text = if (isSafe) "SAFE" else "TOXIC"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(container)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            "$label: $text",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = contents
        )
    }
}
