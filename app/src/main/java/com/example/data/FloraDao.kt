package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FloraDao {
    // --- Garden queries ---
    @Query("SELECT * FROM plants ORDER BY lastWateredDate DESC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    fun getPlantById(id: Int): Flow<Plant?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Update
    suspend fun updatePlant(plant: Plant)

    @Delete
    suspend fun deletePlant(plant: Plant)

    // --- Diagnostic queries ---
    @Query("SELECT * FROM diagnoses ORDER BY diagnosedDate DESC")
    fun getAllDiagnoses(): Flow<List<Diagnosis>>

    @Query("SELECT * FROM diagnoses WHERE plantId = :plantId ORDER BY diagnosedDate DESC")
    fun getDiagnosesForPlant(plantId: Int): Flow<List<Diagnosis>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnosis(diagnosis: Diagnosis): Long

    @Delete
    suspend fun deleteDiagnosis(diagnosis: Diagnosis)
}
