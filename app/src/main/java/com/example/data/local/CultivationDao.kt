package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.FertilizingEntity
import com.example.data.model.HarvestingEntity
import com.example.data.model.LandEntity
import com.example.data.model.LandWithCultivationRecords
import com.example.data.model.PlantingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivationDao {

    // --- Land Queries ---
    @Query("SELECT * FROM lands ORDER BY createdAt ASC")
    fun observeAllLands(): Flow<List<LandEntity>>

    @Transaction
    @Query("SELECT * FROM lands ORDER BY createdAt ASC")
    fun observeAllLandsWithRecords(): Flow<List<LandWithCultivationRecords>>

    @Transaction
    @Query("SELECT * FROM lands WHERE id = :landId")
    fun observeLandWithRecords(landId: Long): Flow<LandWithCultivationRecords?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLand(land: LandEntity): Long

    @Update
    suspend fun updateLand(land: LandEntity)

    @Query("DELETE FROM lands WHERE id = :landId")
    suspend fun deleteLandById(landId: Long)

    @Query("DELETE FROM lands")
    suspend fun deleteAllLands()

    // --- Planting Queries ---
    @Query("SELECT * FROM plantings ORDER BY plantingDate DESC, createdAt DESC")
    fun observeAllPlantings(): Flow<List<PlantingEntity>>

    @Query("SELECT * FROM plantings WHERE landId = :landId ORDER BY plantingDate DESC")
    fun observePlantingsByLand(landId: Long): Flow<List<PlantingEntity>>

    @Query("SELECT * FROM plantings WHERE sourceProgresId = :sourceProgresId LIMIT 1")
    suspend fun getPlantingBySourceProgresId(sourceProgresId: Long): PlantingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanting(planting: PlantingEntity): Long

    @Update
    suspend fun updatePlanting(planting: PlantingEntity)

    @Query("DELETE FROM plantings WHERE id = :plantingId")
    suspend fun deletePlantingById(plantingId: Long)

    @Query("DELETE FROM plantings WHERE sourceProgresId = :sourceProgresId")
    suspend fun deletePlantingBySourceProgresId(sourceProgresId: Long)

    // --- Fertilizing Queries ---
    @Query("SELECT * FROM fertilizings ORDER BY fertilizingDate DESC, createdAt DESC")
    fun observeAllFertilizings(): Flow<List<FertilizingEntity>>

    @Query("SELECT * FROM fertilizings WHERE landId = :landId ORDER BY fertilizingDate DESC")
    fun observeFertilizingsByLand(landId: Long): Flow<List<FertilizingEntity>>

    @Query("SELECT * FROM fertilizings WHERE sourceProgresId = :sourceProgresId LIMIT 1")
    suspend fun getFertilizingBySourceProgresId(sourceProgresId: Long): FertilizingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFertilizing(fertilizing: FertilizingEntity): Long

    @Update
    suspend fun updateFertilizing(fertilizing: FertilizingEntity)

    @Query("DELETE FROM fertilizings WHERE id = :fertilizingId")
    suspend fun deleteFertilizingById(fertilizingId: Long)

    @Query("DELETE FROM fertilizings WHERE sourceProgresId = :sourceProgresId")
    suspend fun deleteFertilizingBySourceProgresId(sourceProgresId: Long)

    // --- Harvesting Queries ---
    @Query("SELECT * FROM harvestings ORDER BY harvestDate DESC, createdAt DESC")
    fun observeAllHarvestings(): Flow<List<HarvestingEntity>>

    @Query("SELECT * FROM harvestings WHERE landId = :landId ORDER BY harvestDate DESC")
    fun observeHarvestingsByLand(landId: Long): Flow<List<HarvestingEntity>>

    @Query("SELECT * FROM harvestings WHERE sourceProgresId = :sourceProgresId LIMIT 1")
    suspend fun getHarvestingBySourceProgresId(sourceProgresId: Long): HarvestingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvesting(harvesting: HarvestingEntity): Long

    @Update
    suspend fun updateHarvesting(harvesting: HarvestingEntity)

    @Query("DELETE FROM harvestings WHERE id = :harvestingId")
    suspend fun deleteHarvestingById(harvestingId: Long)

    @Query("DELETE FROM harvestings WHERE sourceProgresId = :sourceProgresId")
    suspend fun deleteHarvestingBySourceProgresId(sourceProgresId: Long)
}
