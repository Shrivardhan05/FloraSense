package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.Diagnosis
import com.example.data.FloraDatabase
import com.example.data.FloraRepository
import com.example.data.Plant
import com.example.network.Content
import com.example.network.GeminiApiClient
import com.example.network.GenerateContentRequest
import com.example.network.GenerationConfig
import com.example.network.InlineData
import com.example.network.Part
import com.example.network.PlantScanResult
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import android.util.Base64

sealed interface ScanUiState {
    object Idle : ScanUiState
    object Scanning : ScanUiState
    data class Success(val result: PlantScanResult, val isMocked: Boolean) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

class FloraViewModel(application: Application) : AndroidViewModel(application) {
    private val database = FloraDatabase.getDatabase(application)
    private val repository = FloraRepository(database.floraDao())

    // Live list of saved plants from Room Database
    val plants: StateFlow<List<Plant>> = repository.allPlants.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Live list of diagnoses
    val diagnoses: StateFlow<List<Diagnosis>> = repository.allDiagnoses.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current app global season setting (user can toggle this live on the UI to see adaptive intervals alter dynamically!)
    private val _currentAppSeason = MutableStateFlow("Spring")
    val currentAppSeason: StateFlow<String> = _currentAppSeason.asStateFlow()

    // Global Pro User state flow
    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    // Active scan UI state
    private val _scanState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val scanState: StateFlow<ScanUiState> = _scanState.asStateFlow()

    // Water tracker logs simulation trigger helper
    fun setSeason(season: String) {
        _currentAppSeason.value = season
        // Update all existing plants in the database to align with the chosen season
        viewModelScope.launch(Dispatchers.IO) {
            val currentPlants = plants.value
            for (plant in currentPlants) {
                repository.updatePlant(plant.copy(currentSeason = season))
            }
        }
    }

    fun toggleProUser() {
        _isProUser.value = !_isProUser.value
    }

    fun resetScanState() {
        _scanState.value = ScanUiState.Idle
    }

    // --- Database Operations ---

    fun savePlantToGarden(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertPlant(plant)
        }
    }

    fun waterPlant(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = plant.copy(
                lastWateredDate = System.currentTimeMillis(),
                healthStatus = "Healthy" // Reset/Improve health state upon watering
            )
            repository.updatePlant(updated)
        }
    }

    fun updatePlantJournal(plant: Plant, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePlant(plant.copy(note = note))
        }
    }

    fun removePlant(plant: Plant) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlant(plant)
        }
    }

    fun deleteDiagnosisHistory(diagnosis: Diagnosis) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDiagnosis(diagnosis)
        }
    }

    // --- Gemini scanning implementation ---

    fun scanPlant(bitmap: Bitmap?, customPrompt: String? = null, fallbackTemplate: String? = null) {
        _scanState.value = ScanUiState.Scanning

        viewModelScope.launch {
            val apiKey = BuildConfig.GEMINI_API_KEY
            val hasValidKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

            if (hasValidKey && bitmap != null) {
                try {
                    val result = callGeminiApiForScanner(bitmap, customPrompt)
                    if (result != null) {
                        _scanState.value = ScanUiState.Success(result, isMocked = false)
                    } else {
                        _scanState.value = ScanUiState.Error("Received empty analysis response from Gemini.")
                    }
                } catch (e: Exception) {
                    Log.e("FloraSense", "Gemini Scan Failed, fallback to mock database", e)
                    // Gracefully fallback to high-fidelity mocks so the app behaves beautifully
                    val mockResult = getMockResponseForPlant(fallbackTemplate ?: "monstera")
                    _scanState.value = ScanUiState.Success(mockResult, isMocked = true)
                }
            } else {
                // If api key is empty, simulate intelligence with high-fidelity template match instantly after brief scan delay
                withContext(Dispatchers.IO) {
                    Thread.sleep(1500) // Realistic scanning delay animation duration
                }
                val mockResult = getMockResponseForPlant(fallbackTemplate ?: "monstera")
                _scanState.value = ScanUiState.Success(mockResult, isMocked = true)
            }
        }
    }

    private suspend fun callGeminiApiForScanner(bitmap: Bitmap, customPrompt: String?): PlantScanResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val base64Image = bitmap.toBase64()

        val prompt = """
            Analyze this plant image and provide detailed species identification and disease diagnostics in a strictly valid JSON response.
            Do not include any markdown backticks in your reply (e.g., do not wrap in ```json ... ```), write only raw valid JSON code.
            
            Return this exact JSON schema:
            {
              "species": "Standard scientific name and popular name (e.g. Monstera Deliciosa (Swiss Cheese Plant))",
              "healthStatus": "Healthy" or "Needs Care" or "Critical",
              "issueDetected": "Summary of any disease, pest infestation, or environmental issue spotted. Write 'None' if perfectly healthy.",
              "confidence": integer between 1 and 100 matching diagnostic reliability,
              "fixes": "Step 1: ...\nStep 2: ...\nStep 3: ... (newline-separated steps to treatment or care optimization)",
              "careInstructions": "Brief general care instructions focusing on watering schedule and sunlight to avoid this issue",
              "humidityNeed": "Low" or "Medium" or "High",
              "lightNeed": "Low Shade" or "Indirect Light" or "Full Sun",
              "tempNeed": "E.g. 18-25°C or Warm or Moderate",
              "isPetToxic": boolean (true if toxic to cats or dogs),
              "toxicityNotes": "Description of why it is harmful (e.g. Contains toxic calcium oxalate crystals causing ingestion irritation) or 'Safe for cats and dogs.'"
            }
            
            Additional prompt context user supplied (if any): ${customPrompt ?: ""}
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f
            )
        )

        try {
            val response = GeminiApiClient.service.generateContent(apiKey, request)
            val jsonResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonResponse != null) {
                // Parse returned JSON with Moshi
                val adapter = GeminiApiClient.moshiInstance.adapter(PlantScanResult::class.java)
                adapter.fromJson(jsonResponse)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FloraSense", "Json Parsing/API issue in Gemini Client code", e)
            throw e
        }
    }

    // Helper to convert Bitmap to Base64
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    // --- High Fidelity Plant Outlines Database ---

    private fun getMockResponseForPlant(key: String): PlantScanResult {
        return when (key.lowercase()) {
            "monstera" -> PlantScanResult(
                species = "Monstera Deliciosa (Swiss Cheese Plant)",
                healthStatus = "Healthy",
                issueDetected = "None",
                confidence = 97,
                fixes = "Step 1: Feed balanced liquid organic fertilizer monthly during warm season.\nStep 2: Wipe wide glossy leaves clean with damp cloth to facilitate light intake.\nStep 3: Provide a moss pole for aerial roots support as it climbs.",
                careInstructions = "Thrives in warm interiors with bright unfiltered indirect light. Water thoroughly only when top 2 inches of potting mix feel dry.",
                humidityNeed = "High",
                lightNeed = "Indirect Light",
                tempNeed = "18-29°C",
                isPetToxic = true,
                toxicityNotes = "Contains sharp insoluble calcium oxalate crystals. Causes severe oral irritation, drooling, and difficulty swallowing if ingested by pets."
            )
            "fiddle" -> PlantScanResult(
                species = "Ficus Lyrata (Fiddle Leaf Fig)",
                healthStatus = "Needs Care",
                issueDetected = "Root Rot (Overwatering Leaf Drops)",
                confidence = 85,
                fixes = "Step 1: Stop watering immediately and check drainage holes.\nStep 2: Repot in chunky well-draining soil mix containing perlite and orchid bark.\nStep 3: Prune any dark brown mushy rotten roots containing sour odor.",
                careInstructions = "Needs very consistent light. Position within 3 feet of a large sunny window. Let dry almost completely between waterings.",
                humidityNeed = "Medium",
                lightNeed = "Full Sun",
                tempNeed = "18-24°C",
                isPetToxic = true,
                toxicityNotes = "Sap contains irritating latex compounds. Causes severe skin rashes, dermal inflammation, and gastrointestinal vomiting in both cats and dogs."
            )
            "snake" -> PlantScanResult(
                species = "Sansevieria Trifasciata (Snake Plant)",
                healthStatus = "Healthy",
                issueDetected = "None",
                confidence = 98,
                fixes = "Step 1: Avoid excessive frequency of watering; less is more.\nStep 2: Keep in a porous terracotta planter to wick residual soil moisture.\nStep 3: Repot only when container roots crack or push visible bulges.",
                careInstructions = "Extremely drought-tolerant plant. Thrives in dry air. Place in virtually any lighting condition, from dark hallways to intense sun.",
                humidityNeed = "Low",
                lightNeed = "Low Shade",
                tempNeed = "15-32°C",
                isPetToxic = true,
                toxicityNotes = "Contains poison saponins. Chewing leaves prompts local numbing, nausea, vomiting, or excessive salivation in pets."
            )
            "fern" -> PlantScanResult(
                species = "Nephrolepis Exaltata (Boston Fern)",
                healthStatus = "Needs Care",
                issueDetected = "Spider Mite Infestation (Leaf Yellowing)",
                confidence = 81,
                fixes = "Step 1: Isolate from other plants to curb migration.\nStep 2: Hose or shower foliage thoroughly with lukewarm water to physically displace mites.\nStep 3: Spray organic Neem Oil or insecticidal soap diluted daily for 2 weeks.",
                careInstructions = "Prefers cool humid air, moist organic humus soils, and dim indirect light. Mist leaves twice weekly to stave off mites.",
                humidityNeed = "High",
                lightNeed = "Low Shade",
                tempNeed = "16-22°C",
                isPetToxic = false,
                toxicityNotes = "100% Non-toxic. Safe and pet-friendly. Excellent foliage companion for playful curious cats and dogs."
            )
            "lily" -> PlantScanResult(
                species = "Spathiphyllum (Peace Lily)",
                healthStatus = "Critical",
                issueDetected = "Underwatered Severe Foliage Droop",
                confidence = 94,
                fixes = "Step 1: Submerge potty container fully in sink/bucket filled with lukewarm water for 25 minutes (bottom watering).\nStep 2: Let gravity drain excess water completely out of basal tray.\nStep 3: Trim dead dry crisped brown margin leaves at base with sterile snips.",
                careInstructions = "Will dramatic-droop whenthirsty, but recovers and stands straight within 2 hours of hydration. Tolerates ambient shade beautifully.",
                humidityNeed = "High",
                lightNeed = "Low Shade",
                tempNeed = "18-26°C",
                isPetToxic = true,
                toxicityNotes = "Produces painful calcium oxalates. Highly noxious to children and pets, leading to mouth burning, drooling, or localized throat swelling."
            )
            else -> PlantScanResult(
                species = "Chlorophytum Comosum (Spider Plant)",
                healthStatus = "Healthy",
                issueDetected = "None",
                confidence = 96,
                fixes = "Step 1: Trim browned leafy tips which usually result from tap water fluorides.\nStep 2: Collect offsets ('spiderettes') hanging from runners to propagate in water jars.\nStep 3: Feed sparse organic fertilizer during spring bursts.",
                careInstructions = "Very resilient climber/hanger. Thrives in medium indirect views. Prefers watering when top soil is fully dry.",
                humidityNeed = "Medium",
                lightNeed = "Indirect Light",
                tempNeed = "15-25°C",
                isPetToxic = false,
                toxicityNotes = "Completely safe, non-toxic, and acts as a marvelous air-purifier for children's bedrooms."
            )
        }
    }
}
