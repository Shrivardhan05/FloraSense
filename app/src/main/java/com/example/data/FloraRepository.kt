package com.example.data

import kotlinx.coroutines.flow.Flow

class FloraRepository(private val floraDao: FloraDao) {
    val allPlants: Flow<List<Plant>> = floraDao.getAllPlants()
    val allDiagnoses: Flow<List<Diagnosis>> = floraDao.getAllDiagnoses()

    fun getPlantById(id: Int): Flow<Plant?> {
         return floraDao.getPlantById(id)
    }

    fun getDiagnosesForPlant(plantId: Int): Flow<List<Diagnosis>> {
         return floraDao.getDiagnosesForPlant(plantId)
    }

    suspend fun insertPlant(plant: Plant): Long {
         return floraDao.insertPlant(plant)
    }

    suspend fun updatePlant(plant: Plant) {
         floraDao.updatePlant(plant)
    }

    suspend fun deletePlant(plant: Plant) {
         floraDao.deletePlant(plant)
    }

    suspend fun insertDiagnosis(diagnosis: Diagnosis): Long {
         return floraDao.insertDiagnosis(diagnosis)
    }

    suspend fun deleteDiagnosis(diagnosis: Diagnosis) {
         floraDao.deleteDiagnosis(diagnosis)
    }
}
