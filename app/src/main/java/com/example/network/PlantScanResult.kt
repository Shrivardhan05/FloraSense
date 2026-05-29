package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlantScanResult(
    val species: String,
    val healthStatus: String,      // "Healthy", "Needs Care", "Critical"
    val issueDetected: String,     // Diagnosis detail, e.g., "Manganese Deficiency" or "None"
    val confidence: Int,           // e.g. 92
    val fixes: String,             // Newline-separated lists of instructions
    val careInstructions: String,  // How to optimize environment
    val humidityNeed: String,      // "Low", "Medium", "High"
    val lightNeed: String,         // "Low Shade", "Indirect Light", "Full Sun"
    val tempNeed: String,          // e.g. "18-24°C" or "Warm"
    val isPetToxic: Boolean,       // Toxicity for pets
    val toxicityNotes: String      // Additional warning notes
)
