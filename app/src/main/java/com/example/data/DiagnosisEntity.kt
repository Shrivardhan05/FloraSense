package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnoses")
data class Diagnosis(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val plantId: Int? = null,      // Optional associated plant in user garden
    val species: String,           // Identified plant species name
    val diagnosedDate: Long = System.currentTimeMillis(),
    val imageUrl: String,          // Captured picture path or resource url
    val issueDetected: String,     // "Healthy" or diagnoses e.g. "Spider Mites Infestation"
    val confidence: Int,           // Confidence percentage e.g., 88%
    val fixes: String,             // Newline separated step-by-step healing guide
    val beforeImageUrl: String,    // Current infected image
    val afterImageUrl: String,     // Prediction of healed plant visual (before/after slider)
    val careInstructions: String = "" // Watering and environmental changes to prevent recurrence
) {
    fun getFixesList(): List<String> {
        if (fixes.isBlank()) return emptyList()
        return fixes.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
