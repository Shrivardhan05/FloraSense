package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val species: String,
    val customName: String,
    val imageUrl: String,
    val wateringIntervalDays: Int,
    val lastWateredDate: Long,
    val healthStatus: String,      // "Healthy", "Needs Care", "Critical"
    val humidityNeed: String,      // "Low", "Medium", "High"
    val lightNeed: String,         // "Low Shade", "Indirect Light", "Full Sun"
    val tempNeed: String,          // e.g. "18-24°C" or "Moderate"
    val isPetToxic: Boolean,       // Alerts pet-owners
    val toxicityNotes: String,     // Detail on cat/dog response
    val potSize: String,           // "Small", "Medium", "Large"
    val currentSeason: String,     // "Spring", "Summer", "Autumn", "Winter"
    val note: String = ""          // User journals
) {
    // Computes next care/watering date dynamically using the adaptive rules
    fun getNextWateringTimestamp(): Long {
        var multiplier = 1.0f

        // 1. Pot size affects evaporation rate
        when (potSize.lowercase()) {
            "small" -> multiplier *= 0.8f  // Dries out quicker -> water sooner
            "large" -> multiplier *= 1.3f  // Retains water longer -> hold off water
        }

        // 2. Season impacts photosynthesis and growth
        when (currentSeason.lowercase()) {
            "summer" -> multiplier *= 0.7f // Urgent heat -> water more frequently
            "winter" -> multiplier *= 1.6f // Sleep cycle -> water much less
        }

        val adjustedIntervalDays = (wateringIntervalDays * multiplier).coerceAtLeast(1.0f)
        val msInDay = 24 * 60 * 60 * 1000L
        return lastWateredDate + (adjustedIntervalDays * msInDay).toLong()
    }

    // Days remaining until care is due
    fun getDaysUntilWatering(): Int {
        val remainingMs = getNextWateringTimestamp() - System.currentTimeMillis()
        return (remainingMs / (24 * 60 * 60 * 1000L)).toInt()
    }
}
